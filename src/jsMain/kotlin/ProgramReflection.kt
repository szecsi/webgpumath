package vision.gears.webgpumath

import kotlin.js.json

/**
 * What a pair of vertex and fragment shaders needs from the outside, and how to supply it.
 *
 * - On construction, it builds the bind group layouts from the resource variables
 *   the shaders actually use. Variables that are only declared (as everything in
 *   the Prelude is, in every shader) need no provider.
 * - gatherUniforms creates, in a UniformProvider, the uniform objects for the
 *   variables that provider claims.
 * - pipeline gives the render pipeline for drawing a geometry with this program.
 * - bind finds the provider of every variable among the given uniform providers,
 *   uploads their data and sets the bind groups.
 *
 * Drawing a geometry with a program is: set the pipeline, bind, then `geometry.draw`.
 *
 * Conventions in WGSL:
 * - `var<uniform> material : Material` belongs to the provider that claims "material".
 * - `var material_colorTexture : texture_2d<f32>` is the texture `colorTexture` of
 *   provider "material" (textures cannot be struct members in WGSL).
 * - `var material_colorTextureSampler : sampler` receives the sampler of the texture
 *   bound to `material_colorTexture`.
 */
open class ProgramReflection(
  val device : GPUDevice,
  val vertexModule : GPUShaderModule,
  val vertex : ShaderReflection,
  val fragmentModule : GPUShaderModule,
  val fragment : ShaderReflection,
  val debugName : String = "${vertex.debugName}+${fragment.debugName}" ) {

  /** Unique among programs. A RenderQueue can sort by it. */
  val id = nextResourceId()

  val vertexEntryPoint = vertex.vertexEntryPoint ?: throw Error("Shader ${vertex.debugName} has no @vertex function.")
  val fragmentEntryPoint = fragment.fragmentEntryPoint ?: throw Error("Shader ${fragment.debugName} has no @fragment function.")
  val vertexInputs : List<VertexInput> = vertex.vertexInputs

  /** The used resource variables by name. */
  val bindings = HashMap<String, BindingInfo>()
  /** The used resource variables of each bind group, in binding order. */
  val groups : Array<List<BindingInfo>>
  private val groupSignatures : Array<String>
  private val groupLayouts : Array<GPUBindGroupLayout>
  private val pipelineLayout : GPUPipelineLayout
  /** For each sampler variable, the texture variable whose sampler it receives. */
  private val textureOfSampler = HashMap<String, BindingInfo>()
  private val pipelines = HashMap<String, GPURenderPipeline>()
  private val warned = HashSet<String>()

  init {
    for(shader in arrayOf(vertex, fragment)) {
      for(name in shader.used) {
        val binding = shader.bindings[name] ?: continue
        val other = bindings[name]
        if(other != null && other.signature != binding.signature) {
          throw Error("WGSL variable '$name' is declared differently in ${vertex.debugName} and ${fragment.debugName}.")
        }
        bindings[name] = binding
      }
    }
    for(binding in bindings.values) {
      if(binding.kind == BindingKind.TEXTURE && !binding.name.contains('_')) {
        throw Error("Texture variable '${binding.name}' in $debugName must be named <provider>_<name>, e.g. material_colorTexture.")
      }
      if(binding.kind == BindingKind.SAMPLER) {
        val textureName = binding.name.removeSuffix("Sampler")
        textureOfSampler[binding.name] = vertex.bindings[textureName] ?: fragment.bindings[textureName]
          ?: throw Error("Sampler variable '${binding.name}' in $debugName must be named <texture variable>Sampler, but there is no texture variable '$textureName'.")
      }
    }

    val groupCount = (bindings.values.maxOfOrNull { it.group } ?: -1) + 1
    groups = Array(groupCount) { g -> bindings.values.filter { it.group == g }.sortedBy { it.binding } }
    groupSignatures = Array(groupCount) { g -> groups[g].joinToString(";") { layoutEntrySignature(it) } }
    groupLayouts = Array(groupCount) { g ->
      layoutCache.getOrPut(groupSignatures[g]) {
        device.createBindGroupLayout(json("entries" to groups[g].map { layoutEntry(it) }.toTypedArray()))
      }
    }
    pipelineLayout = device.createPipelineLayout(json("bindGroupLayouts" to groupLayouts))
  }

  private fun layoutEntrySignature(b : BindingInfo) = "${b.binding}:${b.kind}:${if(b.kind == BindingKind.TEXTURE || b.kind == BindingKind.SAMPLER) b.typeName else ""}"

  private fun layoutEntry(b : BindingInfo) : dynamic {
    val entry = json(
      "binding" to b.binding,
      "visibility" to (GPUShaderStage.VERTEX or GPUShaderStage.FRAGMENT))
    when(b.kind) {
      BindingKind.UNIFORM -> entry["buffer"] = json("type" to "uniform")
      BindingKind.STORAGE -> entry["buffer"] = json("type" to "read-only-storage")
      BindingKind.TEXTURE -> entry["texture"] = json(
        "sampleType" to textureSampleType(b.typeName),
        "viewDimension" to textureViewDimension(b.typeName))
      BindingKind.SAMPLER -> entry["sampler"] = json(
        "type" to if(b.typeName == "sampler_comparison") "comparison" else "filtering")
    }
    return entry
  }

  private fun textureSampleType(typeName : String) : String = when {
    typeName.startsWith("texture_depth") -> "depth"
    typeName.endsWith("<i32>") -> "sint"
    typeName.endsWith("<u32>") -> "uint"
    else -> "float"
  }

  /** Creates, in `target`, the uniforms of the variables of this program that `target` claims. */
  fun gatherUniforms(target : UniformProvider) {
    for(name in target.variableNames) {
      val binding = bindings[name]
      if(binding != null && binding.kind == BindingKind.UNIFORM && !binding.isArray) {
        val layout = binding.struct!!
        val block = target.blocks[name]
        if(block == null) {
          target.addBlock(name, layout)
        } else if(block.layout.signature != layout.signature) {
          throw Error("WGSL struct of variable '$name' in $debugName differs from the one ${target::class.simpleName} already has: ${layout.signature} vs. ${block.layout.signature}. A provider has one layout; shaders sharing it must declare the struct identically.")
        }
      }
      for(texture in bindings.values) {
        if(texture.kind != BindingKind.TEXTURE || texture.name.substringBefore('_') != name) continue
        val memberName = texture.name.substringAfter('_')
        val reflected = makeTextureUniform(texture.typeName, memberName)
        val existing = target.uniforms[memberName]
        if(existing == null) {
          target.uniforms[memberName] = reflected
        } else if(existing::class != reflected::class) {
          throw Error("Texture '$memberName' of ${target::class.simpleName} is a ${existing::class.simpleName} in Kotlin, but WGSL variable '${texture.name}' in $debugName is a ${texture.typeName}.")
        }
      }
    }
  }

  private fun warnOnce(message : String) {
    if(warned.add(message)) console.warn(message)
  }

  private fun textureFor(binding : BindingInfo, pass : Pass, providers : Array<out UniformProvider>) : Texture {
    val providerName = binding.name.substringBefore('_')
    val memberName = binding.name.substringAfter('_')
    val provider = providers.firstOrNull { providerName in it.variableNames }
    if(provider == null) {
      warnOnce("No uniform provider '$providerName' for texture variable '${binding.name}' when drawing with $debugName.")
    }
    val texture = provider?.textureFor(memberName)
    if(provider != null && texture == null) {
      warnOnce("No texture set for '$memberName' of '$providerName' when drawing with $debugName.")
    }
    return texture ?: TextureTools.placeholder(pass.device, textureViewDimension(binding.typeName))
  }

  /** Uploads and binds the resources of all used variables. */
  fun bind(pass : Pass, vararg uniformProviders : UniformProvider) {
    for(g in groups.indices) {
      val entries = ArrayList<dynamic>()
      val key = StringBuilder(groupSignatures[g])
      for(binding in groups[g]) {
        val resource : dynamic
        val resourceId : Int
        when(binding.kind) {
          BindingKind.UNIFORM, BindingKind.STORAGE -> {
            val provider = uniformProviders.firstOrNull { binding.name in it.variableNames }
              ?: throw Error("No uniform provider for WGSL variable '${binding.name}' when drawing with $debugName. Providers passed: ${uniformProviders.joinToString { it::class.simpleName + it.variableNames.joinToString(",", "[", "]") }}")
            val buffer = provider.bufferFor(binding.name, pass)
            resource = json("buffer" to buffer.buffer)
            resourceId = buffer.resourceId
          }
          BindingKind.TEXTURE -> {
            val texture = textureFor(binding, pass, uniformProviders)
            resource = texture.view
            resourceId = texture.resourceId
          }
          BindingKind.SAMPLER -> {
            val texture = textureFor(textureOfSampler[binding.name]!!, pass, uniformProviders)
            resource = texture.sampler
            resourceId = texture.resourceId
          }
        }
        key.append('|').append(resourceId)
        entries += json("binding" to binding.binding, "resource" to resource)
      }
      val bindGroup = bindGroupCache.getOrPut(key.toString()) {
        device.createBindGroup(json("layout" to groupLayouts[g], "entries" to entries.toTypedArray()))
      }
      pass.encoder.setBindGroup(g, bindGroup)
    }
  }

  /**
   * The render pipeline for drawing `geometry` with this program into the targets of `pass`.
   * `state` is the depth test, culling and blending. Pipelines are created once, then reused.
   */
  fun pipeline(pass : Pass, geometry : Geometry, state : RenderState = RenderState.default) : GPURenderPipeline {
    // an index buffer of strips needs a word in the pipeline, too
    val stripIndexFormat = if(geometry.indexBuffer != null && geometry.topology.endsWith("-strip")) geometry.indexFormat else null
    val key = "${geometry.layoutKey}|$stripIndexFormat|${pass.targetKey}|${state.key}"
    return pipelines.getOrPut(key) {
      state.validate()
      // Every part of the descriptor is merged: defaults first, then what the render state sets,
      // then what is not the render state's to decide.
      val merge : dynamic = js("Object.assign")
      val buffers = vertexInputs.map { input ->
        val attribute = geometry.vertexAttributes[input.name]
          ?: throw Error("Shader ${vertex.debugName} needs vertex attribute '${input.name}', but ${geometry::class.simpleName} only has [${geometry.vertexAttributes.keys.joinToString()}].")
        json(
          "arrayStride" to attribute.stride,
          "attributes" to arrayOf(json(
            "shaderLocation" to input.location,
            "offset" to 0,
            "format" to attribute.format)))
      }.toTypedArray()
      val targets = pass.colorFormats.mapIndexed { index, format ->
        merge(json(), state.target, state.targetOrNull(index) ?: json(), json("format" to format))
      }.toTypedArray()
      val primitive = merge(json("cullMode" to "none"), state.primitive, json("topology" to geometry.topology))
      if(stripIndexFormat != null) primitive["stripIndexFormat"] = stripIndexFormat
      val multisample = merge(json(), state.multisample, json("count" to pass.sampleCount))
      // alpha to coverage needs samples: a material that asks for it still works in a single-sample pass
      if(pass.sampleCount == 1) multisample["alphaToCoverageEnabled"] = false
      val descriptor = json(
        "label" to debugName,
        "layout" to pipelineLayout,
        "vertex" to json(
          "module" to vertexModule,
          "entryPoint" to vertexEntryPoint,
          "buffers" to buffers),
        "fragment" to json(
          "module" to fragmentModule,
          "entryPoint" to fragmentEntryPoint,
          "targets" to targets),
        "primitive" to primitive,
        "multisample" to multisample)
      if(pass.depthFormat != null) {
        descriptor["depthStencil"] = merge(
          json("depthWriteEnabled" to true, "depthCompare" to "less"),
          state.depthStencil,
          json("format" to pass.depthFormat))
      }
      device.createRenderPipeline(descriptor)
    }
  }

  companion object {
    // Equal layouts are shared, and so are the bind groups made with them.
    private val layoutCache = HashMap<String, GPUBindGroupLayout>()
    private val bindGroupCache = HashMap<String, GPUBindGroup>()
  }
}
