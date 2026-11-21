package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.Uint8Array
import kotlin.js.json

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