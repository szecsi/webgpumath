package vision.gears.webgpumath

import kotlin.js.Json
import kotlin.js.json

/**
 * How something is drawn, apart from the shaders: what WebGL set with gl.enable,
 * gl.depthFunc, gl.blendFunc, gl.cullFace. In WebGPU it is part of the pipeline.
 *
 * A render pipeline descriptor is put together from four sources: the program gives
 * the shaders, the geometry the vertex layout and the topology, the pass the formats
 * and the sample count. The rest is here, in the parts and under the names of
 * GPURenderPipelineDescriptor:
 *
 *     primitive      cullMode, frontFace, unclippedDepth
 *     depthStencil   depthWriteEnabled, depthCompare, stencilFront, stencilBack, stencilReadMask,
 *                    stencilWriteMask, depthBias, depthBiasSlopeScale, depthBiasClamp
 *     multisample    alphaToCoverageEnabled, mask
 *     target         blend, writeMask      (of fragment.targets)
 *
 *     renderState.depthStencil["depthBias"] = 2
 *     renderState.primitive["frontFace"] = "cw"
 *
 * What is not set has its WebGPU default, except: depthCompare is "less" and
 * depthWriteEnabled is true. The most common ones also have properties: cullMode,
 * depthCompare, depthWrite, blend, alphaToCoverage.
 */
class RenderState {
  val primitive : Json = json()
  val depthStencil : Json = json()
  val multisample : Json = json()
  /** For every color target of the pass. */
  val target : Json = json()
  private val targetsByIndex = HashMap<Int, Json>()

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

  // Two states are not in the pipeline, but set while drawing. They stay as they are
  // until set again: give them in every material of a pass that uses stencil or the blend constant.

  /** The value the stencil test compares with, and that the "replace" stencil operation writes. */
  var stencilReference : Int? = null
  /** A GPUColor, e.g. json("r" to 1, "g" to 1, "b" to 1, "a" to 0.5), for the "constant" blend factors. */
  var blendConstant : dynamic = null

  /** Sets the states that are not in the pipeline. Call it when drawing, next to setPipeline. */
  fun applyTo(pass : Pass) {
    val reference = stencilReference
    if(reference != null) pass.encoder.setStencilReference(reference)
    if(blendConstant != null) pass.encoder.setBlendConstant(blendConstant)
  }

  /** Equal for states that give the same pipeline. */
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

  companion object {
    val default = RenderState()

    private val allowedMembers = mapOf(
      "primitive" to setOf("cullMode", "frontFace", "unclippedDepth"),
      "depthStencil" to setOf("depthWriteEnabled", "depthCompare", "stencilFront", "stencilBack",
        "stencilReadMask", "stencilWriteMask", "depthBias", "depthBiasSlopeScale", "depthBiasClamp"),
      "multisample" to setOf("alphaToCoverageEnabled", "mask"),
      "target" to setOf("blend", "writeMask"))

    private val otherOwners = mapOf(
      "primitive" to mapOf("topology" to "the geometry", "stripIndexFormat" to "the geometry"),
      "depthStencil" to mapOf("format" to "the pass"),
      "multisample" to mapOf("count" to "the pass"),
      "target" to mapOf("format" to "the pass"))
  }
}

/**
 * A render pass being recorded, with what drawing into it needs to know:
 * the formats of its targets, and the frame it belongs to.
 */
class Pass(
  val device : GPUDevice,
  val encoder : GPURenderPassEncoder,
  val colorFormats : Array<String>,
  val depthFormat : String?,
  /** Frame counter. Uniform buffers are uploaded once per frame. */
  val frame : Int,
  /** Samples per pixel of the targets: 1, or 4 for multisampling. All attachments of the pass have as many. */
  val sampleCount : Int = 1 ) {

  val targetKey = colorFormats.joinToString(",") + "/" + depthFormat + "/" + sampleCount
}
