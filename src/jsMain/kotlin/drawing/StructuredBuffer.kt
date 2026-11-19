package vision.gears.webgpumath

import org.khronos.webgl.ArrayBuffer
import kotlin.js.json

/**
 * A WGSL storage array of structs, whose elements are objects that live in it:
 *
 *     @group(2) @binding(0) var<storage, read> gameObjects : array<GameObject>;
 *
 * The elements are StructuredElement objects, or any UniformProvider with a block for the
 * array's name (as a material has for its template's `materials` array). Once added,
 * an element's data is in the array's memory: its properties declared with `by Mat4()`
 * etc. are views into the array. Nothing is copied before drawing.
 *
 * The array has no gaps: an element is added at the end, and when one is removed, the
 * last element moves into its place. So an element's `index` changes when another
 * element is removed: read it when it is needed, after the elements of the frame were
 * added and removed.
 *
 * Only the first `count` elements are uploaded. A shader that loops over the array must
 * be told the count in another way, e.g. in a uniform, as `frame.lightCount` tells it for
 * `lights`. A shader that indexes it with what the RenderQueue gives for
 * `@builtin(instance_index)` needs no count: e.g. `gameObjects[instances[instance_index].gameObject]`.
 */
open class StructuredBuffer(val name : String, binding : BindingInfo, capacity : Int = 0) : UniformProvider(name) {

  /** The array declared in the shader prelude as `name`. */
  constructor(name : String, capacity : Int = 0) : this(name,
    Prelude.binding(name)
      ?: throw Error("StructuredBuffer '$name': no such variable in the shader prelude. Load the prelude first, and declare '$name' in it as an array of structs."),
    capacity)

  val layout : StructLayout
  val stride : Int
  /** The number of elements there is room for. Grows when more are added. */
  var capacity : Int
    private set

  init {
    if(binding.kind != BindingKind.STORAGE || !binding.isArray || binding.arrayCount != 0 || binding.struct == null) {
      throw Error("StructuredBuffer '$name': the WGSL variable must be a runtime-sized storage array of structs, `var<storage, read> $name : array<T>`, but it is ${binding.typeName}.")
    }
    layout = binding.struct
    stride = binding.arrayStride
    this.capacity = maxOf(capacity, 1)
  }

  private var data = ArrayBuffer(this.capacity * stride)

  // the elements, in the order they are in the array
  private val elements = ArrayList<UniformProvider>()

  /** The number of elements. */
  val count : Int get() = elements.size

  private fun blockOf(element : UniformProvider) : UniformBlock =
    element.blocks[name] ?: throw Error("StructuredBuffer '$name': ${element::class.simpleName} has no data for an element of '$name'.")

  /** Moves the data of `element` into the array, at the end. Returns its index. */
  fun add(element : UniformProvider) : Int {
    val block = blockOf(element)
    if(block.index >= 0) {
      throw Error("StructuredBuffer '$name': this ${element::class.simpleName} is already an element of an array.")
    }
    if(count == capacity) grow()
    block.moveTo(data, count * stride, stride)
    block.index = count
    elements += element
    return block.index
  }

  /**
   * Takes `element` out of the array: its data moves back into memory of its own.
   * The last element moves into its place.
   */
  fun remove(element : UniformProvider) {
    val block = blockOf(element)
    val i = block.index
    if(i < 0 || i >= count || elements[i] !== element) {
      throw Error("StructuredBuffer '$name': this ${element::class.simpleName} is not an element of it.")
    }
    block.moveOut()
    block.index = -1
    val last = elements.removeAt(count - 1)
    if(last !== element) {
      val lastBlock = blockOf(last)
      lastBlock.moveTo(data, i * stride, stride)
      lastBlock.index = i
      elements[i] = last
    }
  }

  // Doubles the capacity: the elements move into new, larger memory. The GPU buffer is
  // made again at the next draw; as it gets a new resource id, the bind groups that
  // used the old one are not used any more.
  private fun grow() {
    capacity *= 2
    data = ArrayBuffer(capacity * stride)
    for(element in elements) {
      val block = blockOf(element)
      block.moveTo(data, block.index * stride, stride)
    }
    resource?.buffer?.destroy()   // freed once the GPU is done with what was already submitted
    resource = null
    uploadedFrame = -1
  }

  private var resource : BufferResource? = null
  private var uploadedFrame = -1

  override fun bufferFor(name : String, pass : Pass) : BufferResource {
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to this.name,
        "size" to data.byteLength,
        "usage" to (GPUBufferUsage.STORAGE or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    if(uploadedFrame != pass.frame) {
      // uniforms declared before the layout was known live outside the array: copy them in
      for(element in elements) blockOf(element).sync()
      // only the elements there are
      pass.device.queue.writeBuffer(res.buffer, 0, data, 0, maxOf(count, 1) * stride)
      uploadedFrame = pass.frame
    }
    return res
  }
}

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
  val index : Int get() = blocks[buffer.name]!!.index

  /** Takes it out of the buffer; the last element of the buffer moves into its place. */
  fun removeFromBuffer() = buffer.remove(this)

  override fun bufferFor(name : String, pass : Pass) : BufferResource =
    throw Error("StructuredElement '${buffer.name}' is not bound by itself: pass the StructuredBuffer '${buffer.name}' when drawing.")
}
