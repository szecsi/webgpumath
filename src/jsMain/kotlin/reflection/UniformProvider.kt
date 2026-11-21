package vision.gears.webgpumath

/**
 * An object that supplies shader resources. It claims WGSL variables by name:
 * `UniformProvider("camera")` provides the uniform struct `camera` and the
 * textures named `camera_...`.
 *
 * If the variable is declared in the Prelude, the properties declared with
 * `by Vec3()`, `by Mat4()` etc. are views into the provider's uniform buffer from
 * the start, and a property that the WGSL struct does not have is an error.
 * Otherwise the layout comes from the shaders that use it: `program.gatherUniforms(provider)`.
 *
 * A provider only holds data. It is used by passing it along when something is drawn.
 */
open class UniformProvider(vararg val variableNames : String) {
  internal val uniforms = HashMap<String, Uniform>()
  internal val blocks = HashMap<String, UniformBlock>()

  /** True if the layout of everything this provider claims is fixed by the Prelude. */
  protected open val hasFixedLayout : Boolean get() =
    variableNames.isNotEmpty() && variableNames.all { Prelude.binding(it) != null }

  init {
    for(name in variableNames) {
      val binding = Prelude.binding(name) ?: continue
      if(binding.kind == BindingKind.UNIFORM && !binding.isArray) {
        addBlock(name, binding.struct ?: throw Error("No binding struct."))
      }
    }
  }

  internal fun addBlock(name : String, layout : StructLayout) {
    val block = UniformBlock(layout)
    blocks[name] = block
    for(member in layout.members) {
      val view = makeUniformView(member, block.floats, block.ints, 0)
      block.addView(member, view)
      val existing = uniforms[member.name]
      if(existing == null) {
        uniforms[member.name] = view
      } else {
        // declared with `by` before the layout was known: keep the object, copy it at upload
        checkSameType(member.name, existing, view)
        view.set(existing)
        block.detached += existing to view
      }
    }
  }

  protected fun checkSameType(name : String, declared : Uniform, reflected : Uniform) {
    if(declared::class != reflected::class || declared.getStorageSize() != reflected.getStorageSize()) {
      throw Error("Uniform '$name' of ${this::class.simpleName} [${variableNames.joinToString()}] is declared in Kotlin as ${declared::class.simpleName} with element count ${declared.getStorageSize()}, but in WGSL it is a ${reflected::class.simpleName} with element count ${reflected.getStorageSize()}.")
    }
  }

  /**
   * Called by the property delegates (`val position by Vec3()`). Returns the uniform
   * object the property will refer to: the view into the uniform buffer if the layout
   * is known, initialized from `prototype`; otherwise `prototype` itself.
   */
  fun <T : Uniform> bindUniform(name : String, prototype : T) : T {
    val view = uniforms[name]
    if(view != null) {
      checkSameType(name, prototype, view)
      view.set(prototype)
      return view.unsafeCast<T>()
    }
    if(hasFixedLayout && prototype !is UniformSampler) {
      throw Error("${this::class.simpleName} declares uniform '$name', but WGSL variable [${variableNames.joinToString()}] in the shader prelude has no such member.")
    }
    uniforms[name] = prototype
    return prototype
  }

  /**
   * The uniform `name`. There is one for every member of the WGSL structs this provider
   * claims, also if the shaders do not use it at the moment: so no uniform is a mistake,
   * e.g. a misspelt name, and it is an error.
   */
  operator fun get(name : String) : Uniform =
    uniforms[name] ?: throw Error("${this::class.simpleName} [${variableNames.joinToString()}] has no uniform '$name'. The WGSL struct has no such member, or the uniforms were not gathered from shaders that declare it (ShaderProgram.gatherUniforms). It has: [${uniforms.keys.sorted().joinToString()}].")
  operator fun set(name : String, uniform : Uniform ) { get(name).set(uniform) }
  operator fun set(name : String, value : Float ) { get(name).set(value) }

  /** The buffer holding the data of WGSL variable `name`, uploaded if this is its first use in the frame. */
  open fun bufferFor(name : String, pass : Pass) : BufferResource {
    val block = blocks[name] ?: throw Error("${this::class.simpleName} claims WGSL variable '$name', but has no data for it. Declare '$name' in the shader prelude, or gather its uniforms from the shaders that use it (ShaderProgram.gatherUniforms).")
    return block.buffer(pass)
  }

  /**
   * The texture that WGSL variable `<claimed name>_<name>` receives, or null: the one set
   * to this provider's texture uniform `name`. A provider that keeps its textures
   * elsewhere (a material's TextureBindings) answers from there.
   */
  open fun textureFor(name : String) : Texture? = (uniforms[name] as? UniformSampler)?.texture
}
