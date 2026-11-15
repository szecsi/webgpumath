package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Uint8Array
import kotlin.js.json

/**
 * The array-of-structs counterpart of UniformProvider. It claims an array variable
 * declared in the Prelude and owns its memory:
 *
 *     @group(2) @binding(0) var<storage, read> gameObjects : array<GameObject>;   // capacity from the constructor
 *     @group(0) @binding(1) var<uniform> lights : array<Light, 8>;                // capacity 8
 *
 * Objects that live in the array are InstanceElements created over the group.
 * The whole group is uploaded once per frame.
 *
 * In the vertex shader, index a storage array with `@builtin(instance_index)`:
 * drawing an element sets it to the element's slot, and `drawInstanced` draws
 * every slot in one call.
 */
open class InstanceGroup(val name : String, capacity : Int = 0) : UniformProvider(name) {
  private val binding = Prelude.binding(name)
    ?: throw Error("InstanceGroup '$name': no such variable in the shader prelude. Load the prelude first, and declare '$name' in it as an array of structs.")
  val layout : StructLayout
  val stride : Int
  val capacity : Int
  private val isStorage = binding.kind == BindingKind.STORAGE

  init {
    if(!binding.isArray || binding.struct == null) {
      throw Error("InstanceGroup '$name': the WGSL variable must be an array of structs, but it is ${binding.typeName}.")
    }
    layout = binding.struct
    stride = binding.arrayStride
    this.capacity = if(binding.arrayCount > 0) binding.arrayCount else capacity
    if(this.capacity <= 0) {
      throw Error("InstanceGroup '$name': the WGSL array is runtime-sized, so a capacity must be given.")
    }
  }

  val data = ArrayBuffer(this.capacity * stride)
  val floats = Float32Array(data)
  val ints = Int32Array(data)

  private val freeSlots = ArrayList<Int>()
  /** One past the highest slot ever allocated. */
  var extent = 0
    private set

  internal fun allocate() : Int {
    if(freeSlots.isNotEmpty()) return freeSlots.removeAt(freeSlots.size - 1)
    if(extent >= capacity) {
      throw Error("InstanceGroup '$name' is full: capacity is $capacity.")
    }
    return extent++
  }

  internal fun release(slot : Int) {
    Uint8Array(data, slot * stride, stride).asDynamic().fill(0)
    freeSlots += slot
  }

  private var resource : BufferResource? = null
  private var uploadedFrame = -1

  override fun bufferFor(name : String, pass : Pass) : BufferResource {
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to this.name,
        "size" to data.byteLength,
        "usage" to ((if(isStorage) GPUBufferUsage.STORAGE else GPUBufferUsage.UNIFORM) or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    if(uploadedFrame != pass.frame) {
      // a fixed-size uniform array is small: upload all of it, so that released slots read as zero
      val byteCount = if(isStorage) maxOf(extent, 1) * stride else data.byteLength
      pass.device.queue.writeBuffer(res.buffer, 0, data, 0, byteCount)
      uploadedFrame = pass.frame
    }
    return res
  }

  /** Draws `drawable` once for every slot of the group, in a single instanced draw call. */
  fun drawInstanced(pass : Pass, drawable : Drawable, vararg uniformProviders : UniformProvider) {
    if(extent == 0) return
    pass.firstInstance = 0
    pass.instanceCount = extent
    drawable.draw(pass, this, *uniformProviders)
    pass.firstInstance = 0
    pass.instanceCount = 1
  }
}

/**
 * An element of an InstanceGroup. Its properties declared with `by Mat4()`,
 * `by Vec3()` etc. are views into the element's slot in the group's memory.
 * Drawing it draws its components with `instance_index` set to its slot.
 */
open class InstanceElement(val group : InstanceGroup) : UniformProvider(group.name) {
  val slot = group.allocate()
  override val hasFixedLayout : Boolean get() = true
  var isReleased = false
    private set

  init {
    for(member in group.layout.members) {
      uniforms[member.name] = makeUniformView(member, group.floats, group.ints, slot * group.stride)
    }
  }

  /** Gives the slot back to the group. The element must not be used afterwards. */
  fun release() {
    if(!isReleased) {
      isReleased = true
      group.release(slot)
    }
  }

  override fun bufferFor(name : String, pass : Pass) : BufferResource = group.bufferFor(name, pass)

  override fun draw(pass : Pass, vararg uniformProviders : UniformProvider) {
    pass.firstInstance = slot
    pass.instanceCount = 1
    super.draw(pass, *uniformProviders)
    pass.firstInstance = 0
  }

  override fun drawWithOverrides(pass : Pass, overrides : Map<String, UniformProvider>, vararg uniformProviders : UniformProvider) {
    pass.firstInstance = slot
    pass.instanceCount = 1
    super.drawWithOverrides(pass, overrides, *uniformProviders)
    pass.firstInstance = 0
  }
}
