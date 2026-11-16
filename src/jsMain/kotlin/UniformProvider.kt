package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import kotlin.js.json

/** A GPU buffer to be bound, with the id that bind group caching uses. */
class BufferResource(val buffer : GPUBuffer, val resourceId : Int)

/**
 * The CPU-side memory of one WGSL uniform struct and its GPU buffer.
 * The uniform objects of the provider are views into `data`.
 */
class UniformBlock(val layout : StructLayout) {
  val data = ArrayBuffer((layout.size + 15) / 16 * 16)
  val floats = Float32Array(data)
  val ints = Int32Array(data)

  /** Uniforms that were created before the layout was known; copied into `data` before upload. */
  internal val detached = ArrayList<Pair<Uniform, Uniform>>()

  private var resource : BufferResource? = null
  private var uploadedFrame = -1

  fun buffer(pass : Pass) : BufferResource {
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to layout.name,
        "size" to data.byteLength,
        "usage" to (GPUBufferUsage.UNIFORM or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    if(uploadedFrame != pass.frame) {
      detached.forEach { (source, view) -> view.set(source) }
      pass.device.queue.writeBuffer(res.buffer, 0, data)
      uploadedFrame = pass.frame
    }
    return res
  }
}

/**
 * An object that supplies shader resources. It claims WGSL variables by name:
 * `UniformProvider("camera")` provides the uniform struct `camera` and the
 * textures named `camera_...`.
 *
 * If the variable is declared in the Prelude, the properties declared with
 * `by Vec3()`, `by Mat4()` etc. are views into the provider's uniform buffer from
 * the start, and a property that the WGSL struct does not have is an error.
 * Otherwise the layout comes from a shader program: `program.gatherUniforms(provider)`.
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
        addBlock(name, binding.struct!!)
      }
    }
  }

  internal fun addBlock(name : String, layout : StructLayout) {
    val block = UniformBlock(layout)
    blocks[name] = block
    for(member in layout.members) {
      val view = makeUniformView(member, block.floats, block.ints, 0)
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

  fun register(uniformName : String, uniform : Uniform){
    bindUniform(uniformName, uniform)
  }

  operator fun get(name : String): Uniform? {
    if(!uniforms.containsKey(name)){
      console.error("""WARNING: Attempt to access unknown or non-active uniform '${name}'.""" )
    }
    return uniforms[name] }
  operator fun set(name : String, uniform : Uniform ) { (uniforms[name] ?: return).set(uniform) }
  operator fun set(name : String, value : Float ) { (uniforms[name] ?: return).set(value) }

  /** The buffer holding the data of WGSL variable `name`, uploaded if this is its first use in the frame. */
  open fun bufferFor(name : String, pass : Pass) : BufferResource {
    val block = blocks[name] ?: throw Error("${this::class.simpleName} claims WGSL variable '$name', but has no data for it. Declare '$name' in the shader prelude, or gather its uniforms from the program that uses it (program.gatherUniforms).")
    return block.buffer(pass)
  }

  /** The texture bound to this provider's texture uniform `name`, or null. */
  fun textureFor(name : String) : Texture? = (uniforms[name] as? UniformSampler)?.texture
}
