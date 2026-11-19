package vision.gears.webgpumath

import kotlin.js.Json
import kotlin.js.json

/**
 * How something is drawn, apart from the shaders: depth test, culling, blending,
 * stencil, multisampling. In WebGPU it is part of the pipeline.
 *
 * A render pipeline descriptor is put together from four sources: the PipelineTemplate
 * gives the shaders, the VertexLayout of the geometry the attribute formats and the topology,
 * the Target of the pass the formats and the sample count. The rest is here, in the parts
 * and under the names of GPURenderPipelineDescriptor:
 *
 *     primitive      cullMode, frontFace, unclippedDepth
 *     depthStencil   depthWriteEnabled, depthCompare, stencilFront, stencilBack, stencilReadMask,
 *                    stencilWriteMask, depthBias, depthBiasSlopeScale, depthBiasClamp
 *     multisample    alphaToCoverageEnabled, mask
 *     target         blend, writeMask      (of fragment.targets)
 *
 * A render state does not change once made, and equal states are one object. Make one
 * with `RenderState.of`, setting what differs from the defaults, or a changed copy with `with`:
 *
 *     val state = RenderState.of {
 *       cullMode = "back"
 *       depthStencil["depthBias"] = 2
 *     }
 *     val mirrored = state.with { primitive["frontFace"] = "cw" }
 *
 * What is not set has its WebGPU default, except: depthCompare is "less" and
 * depthWriteEnabled is true. The most common ones also have properties: cullMode,
 * depthCompare, depthWrite, blend, alphaToCoverage.
 *
 * The stencil reference and the blend constant are not in the pipeline: they are set
 * while drawing, for each item of a RenderQueue.
 */
class RenderState private constructor(
    private val parts : Builder,
    /** The same for equal states, different for different ones. */
    val key : String) {

  /** Unique among render states. */
  val id = nextResourceId()

  val cullMode get() = parts.cullMode
  val depthCompare get() = parts.depthCompare
  val depthWrite get() = parts.depthWrite
  val blend get() = parts.blend
  val alphaToCoverage get() = parts.alphaToCoverage

  // the parts, for putting together the pipeline descriptor; not to be changed
  internal val primitive : Json get() = parts.primitive
  internal val depthStencil : Json get() = parts.depthStencil
  internal val multisample : Json get() = parts.multisample
  internal val target : Json get() = parts.target
  internal fun targetOrNull(index : Int) : Json? = parts.targetOrNull(index)

  /** The state with what `change` sets changed, and the rest as in this one. */
  fun with(change : Builder.() -> Unit) : RenderState {
    val builder = Builder(this)
    builder.change()
    return of(builder)
  }

  /** The settings of a render state being made. `RenderState.of` and `with` give one to fill in. */
  class Builder() {
    val primitive : Json = json()
    val depthStencil : Json = json()
    val multisample : Json = json()
    /** For every color target of the pass. */
    val target : Json = json()
    private val targetsByIndex = HashMap<Int, Json>()

    /** A copy of the settings of `state`. */
    constructor(state : RenderState) : this() {
      copyFrom(state.parts)
    }

    internal fun copyFrom(from : Builder) {
      copyInto(primitive, from.primitive)
      copyInto(depthStencil, from.depthStencil)
      copyInto(multisample, from.multisample)
      copyInto(target, from.target)
      for((index, part) in from.targetsByIndex) copyInto(target(index), part)
    }

    /** For one color target of the pass only, on top of `target`. */
    fun target(index : Int) : Json = targetsByIndex.getOrPut(index) { json() }
    internal fun targetOrNull(index : Int) : Json? = targetsByIndex[index]

    /** "none", "back" or "front". */
    var cullMode : String
      get() = (primitive["cullMode"] ?: "none").unsafeCast<String>()
      set(value) { primitive["cullMode"] = value }

    /** The depth test: "less", "less-equal", "always", ... */
    var depthCompare : String
      get() = (depthStencil["depthCompare"] ?: "less").unsafeCast<String>()
      set(value) { depthStencil["depthCompare"] = value }

    var depthWrite : Boolean
      get() = (depthStencil["depthWriteEnabled"] ?: true).unsafeCast<Boolean>()
      set(value) { depthStencil["depthWriteEnabled"] = value }

    /** A GPUBlendState object, or null for no blending. */
    var blend : dynamic
      get() = target["blend"] ?: null
      set(value) { target["blend"] = if(value == null) undefined else value }

    /**
     * In a multisampled pass: the alpha of the fragment's color tells how many of the pixel's
     * samples it covers. Transparency without blending and sorting. No effect in a single-sample pass.
     */
    var alphaToCoverage : Boolean
      get() = (multisample["alphaToCoverageEnabled"] ?: false).unsafeCast<Boolean>()
      set(value) { multisample["alphaToCoverageEnabled"] = value }

    /** Equal for settings that give the same pipeline. */
    val key : String get() {
      val text = StringBuilder()
      text.append(JSON.stringify(primitive)).append(JSON.stringify(depthStencil))
      text.append(JSON.stringify(multisample)).append(JSON.stringify(target))
      for(index in targetsByIndex.keys.sorted()) {
        text.append(index).append(JSON.stringify(targetsByIndex[index]))
      }
      return text.toString()
    }

    /**
     * Throws if a part has a member that WebGPU does not know there, or one that is not
     * the render state's to set. WebGPU itself ignores unknown members without a word,
     * so a misspelled name would just have no effect.
     */
    fun validate() {
      validatePart("primitive", primitive)
      validatePart("depthStencil", depthStencil)
      validatePart("multisample", multisample)
      validatePart("target", target)
      targetsByIndex.values.forEach { validatePart("target", it) }
    }

    private fun validatePart(partName : String, part : Json) {
      val allowed = allowedMembers[partName]!!
      val owners = otherOwners[partName]!!
      val keys = js("Object.keys")(part).unsafeCast<Array<String>>()
      for(key in keys) {
        val owner = owners[key]
        if(owner != null) {
          throw Error("RenderState.$partName: '$key' is given by $owner, it cannot be set in a render state.")
        }
        if(key !in allowed) {
          throw Error("RenderState.$partName has no member '$key'. It can have: ${allowed.joinToString()}.")
        }
      }
    }

    // copies the members of `from` into `to`; nested objects (e.g. a blend state) are copied, too
    private fun copyInto(to : Json, from : Json) {
      val copy = JSON.parse<Json>(JSON.stringify(from))
      js("Object.assign")(to, copy)
    }
  }

  companion object {
    private val pool = HashMap<String, RenderState>()

    /** The state that `change` sets up, starting from the defaults: an existing one, or a new one. */
    fun of(change : Builder.() -> Unit) : RenderState {
      val builder = Builder()
      builder.change()
      return of(builder)
    }

    /** The state with the settings of `builder`: an existing one, or a new one. */
    fun of(builder : Builder) : RenderState {
      val key = builder.key
      return pool.getOrPut(key) {
        builder.validate()
        // a copy, so that changing the builder later does not change the state
        val copy = Builder()
        copy.copyFrom(builder)
        RenderState(copy, key)
      }
    }

    private val allowedMembers = mapOf(
      "primitive" to setOf("cullMode", "frontFace", "unclippedDepth"),
      "depthStencil" to setOf("depthWriteEnabled", "depthCompare", "stencilFront", "stencilBack",
        "stencilReadMask", "stencilWriteMask", "depthBias", "depthBiasSlopeScale", "depthBiasClamp"),
      "multisample" to setOf("alphaToCoverageEnabled", "mask"),
      "target" to setOf("blend", "writeMask"))

    private val otherOwners = mapOf(
      "primitive" to mapOf("topology" to "the vertex layout of the geometry", "stripIndexFormat" to "the vertex layout of the geometry"),
      "depthStencil" to mapOf("format" to "the target of the pass"),
      "multisample" to mapOf("count" to "the target of the pass"),
      "target" to mapOf("format" to "the target of the pass"))

    /** Depth test on, no culling, no blending. (Last: making it needs the tables above.) */
    val default = of { }
  }
}
