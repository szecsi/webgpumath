package vision.gears.webgpumath

import kotlin.js.json

/**
 * A vertex and a fragment shader, what they need from the outside, and the render
 * pipelines made from them. A pipeline also depends on the VertexLayout of the geometry
 * (attribute formats, topology), the pass (target formats, sample count) and the render state; the
 * program has one pipeline for every combination that is drawn.
 *
 * - On construction, it builds the bind group layouts from the resource variables
 *   the shaders actually use. Variables that are only declared (as everything in
 *   the Prelude is, in every shader) need no provider.
 * - gatherUniforms creates, in a UniformProvider, the uniform objects for the
 *   variables that provider claims.
 * - pipeline gives the render pipeline for drawing geometries of a VertexLayout with these shaders.
 * - bind finds the provider of every variable among the given uniform providers,
 *   uploads their data and sets the bind groups.
 *
 * Drawing a geometry is: set the pipeline, bind, then `geometry.draw`.
 *
 * `ShaderProgram.of` gives the same program for the same two shader modules,
 * so that everything drawn with the same shaders shares layouts and pipelines.
 *
 * Conventions in WGSL:
 * - `var<uniform> material : Material` belongs to the provider that claims "material".
 * - `var material_colorTexture : texture_2d<f32>` is the texture `colorTexture` of
 *   provider "material" (textures cannot be struct members in WGSL).
 * - `var material_colorTextureSampler : sampler` receives the sampler of the texture
 *   bound to `material_colorTexture`.
 */
class ShaderProgram(
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

  /** The used resource variables by name: these are bound when drawing. */
  val bindings = HashMap<String, BindingInfo>()
  /**
   * All resource variables the two shaders declare, used or not, by name. Uniforms are
   * gathered from these: so a shader that does not use a variable for the moment (e.g.
   * when showing a partial result for testing) does not break the code that sets it.
   */
  val declarations = HashMap<String, BindingInfo>()
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
    // the used ones first: if the two shaders declare an unused variable differently, the used declaration counts
    declarations.putAll(bindings)
    for(shader in arrayOf(vertex, fragment)) {
      for((name, binding) in shader.bindings) {
        if(name !in declarations) declarations[name] = binding
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

  /**
   * Creates, in `target`, the uniforms of the variables of these shaders that `target` claims:
   * of all declared ones, also those the shaders do not use.
   * For an array of structs, `target` gets the memory of one element, to be added to
   * an StructuredBuffer of that name, which then holds it.
   *
   * With `withTextures`, `target` also gets a texture uniform for each texture variable
   * `<claimed name>_<name>`, to set the texture to. A provider that keeps its textures
   * elsewhere (a material's TextureBindings) passes false.
   */
  fun gatherUniforms(target : UniformProvider, withTextures : Boolean = true) {
    for(name in target.variableNames) {
      val binding = declarations[name]
      // a uniform struct, or a storage array of structs, of which the provider holds one element
      val isStruct = binding != null && binding.kind == BindingKind.UNIFORM && !binding.isArray
      val isElement = binding != null && binding.kind == BindingKind.STORAGE && binding.isArray && binding.struct != null
      if(isStruct || isElement) {
        val layout = binding?.struct ?: throw Error("No binding struct for uniform.")
        val block = target.blocks[name]
        if(block == null) {
          target.addBlock(name, layout)
        } else if(block.layout.signature != layout.signature) {
          throw Error("WGSL struct of variable '$name' in $debugName differs from the one ${target::class.simpleName} already has: ${layout.signature} vs. ${block.layout.signature}. A provider has one layout; shaders sharing it must declare the struct identically.")
        }
      }
      if(!withTextures) continue
      for(texture in declarations.values) {
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
            val texture = textureFor(textureOfSampler[binding.name] ?: throw Error("Texture for sampler not found."), pass, uniformProviders)
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
   * The render pipeline for drawing geometries of `layout` with these shaders into the targets
   * of `pass`. `state` is the depth test, culling and blending. Pipelines are created once, then reused.
   */
  fun pipeline(pass : Pass, layout : VertexLayout, state : RenderState = RenderState.default) : GPURenderPipeline {
    val target = pass.target
    // layouts, targets and render states are pooled: equal ones are one object, with one id
    val key = "${layout.id}|${target.id}|${state.id}"
    return pipelines.getOrPut(key) {
      // Every part of the descriptor is merged: defaults first, then what the render state sets,
      // then what is not the render state's to decide.
      val merge : dynamic = js("Object.assign")
      val buffers = vertexInputs.map { input ->
        val format = layout.formats[input.name]
          ?: throw Error("Shader ${vertex.debugName} needs vertex attribute '${input.name}', but the geometry only has [${layout.formats.keys.joinToString()}].")
        json(
          "arrayStride" to VertexLayout.strideOf(format),
          "attributes" to arrayOf(json(
            "shaderLocation" to input.location,
            "offset" to 0,
            "format" to format)))
      }.toTypedArray()
      val targets = target.colorFormats.mapIndexed { index, format ->
        merge(json(), state.target, state.targetOrNull(index) ?: json(), json("format" to format))
      }.toTypedArray()
      val primitive = merge(json("cullMode" to "none"), state.primitive, json("topology" to layout.topology))
      // an indexed strip needs the index format, to tell where the strip restarts
      if(layout.stripIndexFormat != null) primitive["stripIndexFormat"] = layout.stripIndexFormat
      val multisample = merge(json(), state.multisample, json("count" to target.sampleCount))
      // alpha to coverage needs samples: a material that asks for it still works in a single-sample pass
      if(target.sampleCount == 1) multisample["alphaToCoverageEnabled"] = false
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
      if(target.depthFormat != null) {
        descriptor["depthStencil"] = merge(
          json("depthWriteEnabled" to true, "depthCompare" to "less"),
          state.depthStencil,
          json("format" to target.depthFormat))
      }
      device.createRenderPipeline(descriptor)
    }
  }

  companion object {
    // Equal layouts are shared, and so are the bind groups made with them.
    private val layoutCache = HashMap<String, GPUBindGroupLayout>()
    private val bindGroupCache = HashMap<String, GPUBindGroup>()
    private val programs = HashMap<Pair<GPUShaderModule, GPUShaderModule>, ShaderProgram>()

    /** The program of the two shader modules: created on the first call, the same one after. */
    fun of(
        device : GPUDevice,
        vertexModule : GPUShaderModule,
        vertex : ShaderReflection,
        fragmentModule : GPUShaderModule,
        fragment : ShaderReflection ) : ShaderProgram =
      programs.getOrPut(Pair(vertexModule, fragmentModule)) {
        ShaderProgram(device, vertexModule, vertex, fragmentModule, fragment)
      }
  }
}
