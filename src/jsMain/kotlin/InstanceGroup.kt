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
 * an element's `drawInstance` sets it to the element's slot, and `drawInstanced`
 * draws every slot in one call.
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

  /** Whether elements name their own slots (true) or the group hands them out (false). Set by the first element. */
  private var hasClaimedSlots : Boolean? = null

  private fun checkSlotPolicy(claiming : Boolean) {
    if(hasClaimedSlots == null) hasClaimedSlots = claiming
    if(hasClaimedSlots != claiming) {
      throw Error("InstanceGroup '$name': either all elements choose their own slot, or none does.")
    }
  }

  internal fun allocate() : Int {
    checkSlotPolicy(false)
    if(freeSlots.isNotEmpty()) return freeSlots.removeAt(freeSlots.size - 1)
    if(extent >= capacity) {
      throw Error("InstanceGroup '$name' is full: capacity is $capacity.")
    }
    return extent++
  }

  /** Takes a slot chosen by the caller, e.g. the id of the entity the element belongs to. */
  internal fun claim(slot : Int) : Int {
    checkSlotPolicy(true)
    if(slot < 0 || slot >= capacity) {
      throw Error("InstanceGroup '$name': slot $slot is out of range, capacity is $capacity.")
    }
    if(slot >= extent) extent = slot + 1
    return slot
  }

  internal fun release(slot : Int) {
    Uint8Array(data, slot * stride, stride).asDynamic().fill(0)
    // a claimed slot is its claimant's to take again
    if(hasClaimedSlots == false) freeSlots += slot
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
    drawable.draw(pass, 0, extent, this, *uniformProviders)
  }
}

/**
 * An element of an InstanceGroup. Its properties declared with `by Mat4()`,
 * `by Vec3()` etc. are views into the element's slot in the group's memory.
 * `drawInstance` draws something with `instance_index` set to its slot.
 *
 * The group hands out a free slot, unless `claimedSlot` is given. Claim slots when
 * several groups must be indexed by the same number, e.g. an entity id: a draw
 * call has only one `instance_index`.
 */
open class InstanceElement(val group : InstanceGroup, claimedSlot : Int? = null) : UniformProvider(group.name) {
  val slot = if(claimedSlot != null) group.claim(claimedSlot) else group.allocate()
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

  /** Draws `drawable` with `instance_index` set to this element's slot, and this element among the uniform providers. */
  fun drawInstance(pass : Pass, drawable : Drawable, vararg uniformProviders : UniformProvider) {
    drawable.draw(pass, slot, 1, this, *uniformProviders)
  }
}
