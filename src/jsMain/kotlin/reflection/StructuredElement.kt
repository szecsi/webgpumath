package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer

/**
 * One element of a WGSL storage array of structs declared in the Prelude. It is put
 * into `buffer` when it is made, and its properties declared with `by Mat4()`,
 * `by Vec3()` etc. are views into the buffer's memory. It stays there until
 * `removeFromBuffer`: a component calls it when it is disposed of.
 *
 *     class ModelMatrix(override val entity : Int, gameObjects : StructuredBuffer) : StructuredElement(gameObjects), Component {
 *       val modelMatrix by Mat4()
 *       override fun dispose() = removeFromBuffer()
 *     }
 */
open class StructuredElement(val buffer : StructuredBuffer) : UniformProvider(buffer.name) {
  init {
    addBlock(buffer.name, buffer.layout)
    buffer.add(this)
  }

  override val hasFixedLayout : Boolean get() = true

  /** The index of the element in the array; it changes when another element is removed. -1 once removed. */
  val index : Int get() = blocks[buffer.name]?.index ?: -1

  /** Takes it out of the buffer; the last element of the buffer moves into its place. */
  fun removeFromBuffer() = buffer.remove(this)

  override fun bufferFor(name : String, pass : Pass) : BufferResource =
    throw Error("StructuredElement '${buffer.name}' is not bound by itself: pass the StructuredBuffer '${buffer.name}' when drawing.")
}
