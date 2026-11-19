package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Uint8Array
import kotlin.js.json

/** A GPU buffer to be bound, with the id that bind group caching uses. */
class BufferResource(val buffer : GPUBuffer, val resourceId : Int)

/**
 * The CPU-side memory of one WGSL struct, and for a uniform struct its GPU buffer.
 * The uniform objects of the provider are views into it.
 *
 * Its memory is `size` bytes of `data` from `byteOffset`: memory of its own, or one
 * element of an StructuredBuffer. When it moves (`moveTo`), its views are moved with it.
 */
class UniformBlock(val layout : StructLayout) {
  var size = (layout.size + 15) / 16 * 16
    private set
  var data = ArrayBuffer(size)
    private set
  var byteOffset = 0
    private set
  var floats = Float32Array(data)
    private set
  var ints = Int32Array(data)
    private set

  /** Its index in the StructuredBuffer it is an element of; -1 if it is in none. */
  var index = -1
    internal set

  /** The views made for the members of the layout, to move them with the memory. */
  private val views = ArrayList<Pair<MemberLayout, Uniform>>()

  /** Uniforms that were created before the layout was known; copied into `data` before upload. */
  internal val detached = ArrayList<Pair<Uniform, Uniform>>()

  internal fun addView(member : MemberLayout, view : Uniform) {
    views += member to view
  }

  /**
   * Copies the data to `byteCount` bytes of `target` from `targetOffset`, and makes
   * that its memory: the views see it from now on.
   */
  internal fun moveTo(target : ArrayBuffer, targetOffset : Int, byteCount : Int) {
    val copied = minOf(size, byteCount)
    Uint8Array(target, targetOffset, copied).set(Uint8Array(data, byteOffset, copied))
    data = target
    byteOffset = targetOffset
    size = byteCount
    floats = Float32Array(target, targetOffset, byteCount / 4)
    ints = Int32Array(target, targetOffset, byteCount / 4)
    for((member, view) in views) {
      val o = member.offset / 4
      when(view) {
        is UniformFloat -> view.storage = floats.subarray(o, o + view.storage.length)
        is UniformInt -> view.storage = ints.subarray(o, o + view.storage.length)
      }
    }
  }

  /** Moves the data back into memory of its own. */
  internal fun moveOut() {
    moveTo(ArrayBuffer((layout.size + 15) / 16 * 16), 0, (layout.size + 15) / 16 * 16)
  }

  private var resource : BufferResource? = null
  private var uploadedFrame = -1

  /** Brings `data` up to date with the uniforms that live outside it. */
  internal fun sync() {
    detached.forEach { (source, view) -> view.set(source) }
  }

  fun buffer(pass : Pass) : BufferResource {
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to layout.name,
        "size" to size,
        "usage" to (GPUBufferUsage.UNIFORM or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    if(uploadedFrame != pass.frame) {
      sync()
      pass.device.queue.writeBuffer(res.buffer, 0, data, byteOffset, size)
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
 * Otherwise the layout comes from the shaders that use it: `template.gatherUniforms(provider)`.
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

  operator fun get(name : String): Uniform? {
    if(!uniforms.containsKey(name)){
      console.error("""WARNING: Attempt to access unknown or non-active uniform '${name}'.""" )
    }
    return uniforms[name]
  }
  operator fun set(name : String, uniform : Uniform ) { (uniforms[name] ?: return).set(uniform) }
  operator fun set(name : String, value : Float ) { (uniforms[name] ?: return).set(value) }

  /** The buffer holding the data of WGSL variable `name`, uploaded if this is its first use in the frame. */
  open fun bufferFor(name : String, pass : Pass) : BufferResource {
    val block = blocks[name] ?: throw Error("${this::class.simpleName} claims WGSL variable '$name', but has no data for it. Declare '$name' in the shader prelude, or gather its uniforms from the shaders that use it (PipelineTemplate.gatherUniforms).")
    return block.buffer(pass)
  }

  /**
   * The texture that WGSL variable `<claimed name>_<name>` receives, or null: the one set
   * to this provider's texture uniform `name`. A provider that keeps its textures
   * elsewhere (a material's TextureBindings) answers from there.
   */
  open fun textureFor(name : String) : Texture? = (uniforms[name] as? UniformSampler)?.texture
}
