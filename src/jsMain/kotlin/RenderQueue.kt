package vision.gears.webgpumath

import org.khronos.webgl.Uint32Array
import org.khronos.webgl.set
import kotlin.js.json

/** One thing to draw: `drawable`, with the per-object data of `instance`. The rest is for sorting. */
class RenderItem(
  val drawable : Drawable,
  val instance : Int,
  val phase : Int,
  val program : Int,
  val material : Int,
  val geometry : Int,
  val depth : Float )

/**
 * Collects what is to be drawn in a pass, then draws it in an order that needs
 * few state changes, merging neighbours that are the same drawable into one
 * instanced draw call.
 *
 * The instances of such a call are consecutive values of `instance_index`, but the
 * objects drawn are anywhere in their InstanceGroup. So the queue claims an array
 * that maps one to the other, declared in the Prelude:
 *
 *     @group(2) @binding(1) var<storage, read> instanceEntities : array<u32>;
 *
 * and the vertex shader reads `gameObjects[instanceEntities[instance_index]]`.
 *
 * Sorting has several levels:
 * 1. phase: BACKGROUND, OPAQUE, TRANSPARENT, or any other number, in increasing order;
 * 2. in the TRANSPARENT phase, depth, farthest first;
 * 3. program, material, geometry ids, so that equal pipelines and bind groups are neighbours;
 * 4. depth, nearest first, so that the depth test rejects more of what comes later.
 *
 * Every frame: `clear`, `add` everything, then `draw` once. To draw different
 * things in another pass of the same frame, use another queue with its own array.
 */
class RenderQueue(val name : String = "instanceEntities") : UniformProvider(name) {
  companion object {
    const val BACKGROUND = 0
    const val OPAQUE = 1
    const val TRANSPARENT = 2
  }

  init {
    val binding = Prelude.binding(name)
      ?: throw Error("RenderQueue '$name': no such variable in the shader prelude. Load the prelude first, and declare '$name' in it as a storage array<u32>.")
    if(binding.kind != BindingKind.STORAGE || !binding.isArray || binding.struct != null || binding.arrayStride != 4) {
      throw Error("RenderQueue '$name': the WGSL variable must be a storage array<u32>, but it is ${binding.typeName}.")
    }
  }

  private val items = ArrayList<RenderItem>()
  private var instances = Uint32Array(256)
  private var resource : BufferResource? = null
  private var isChanged = false
  private var drawnFrame = -1

  val itemCount get() = items.size
  /** The number of draw calls the last `draw` made. */
  var batchCount = 0
    private set

  fun clear() {
    items.clear()
    isChanged = true
  }

  /**
   * `instance` is the slot of the object's data in its InstanceGroup. `program`, `material`
   * and `geometry` are numbers that tell apart what `drawable` consists of: the `id` of a
   * program and of a geometry, and for a material one from `nextResourceId()`.
   */
  fun add(
      drawable : Drawable,
      instance : Int,
      phase : Int = OPAQUE,
      program : Int = 0,
      material : Int = 0,
      geometry : Int = 0,
      depth : Float = 0.0f ) {
    items += RenderItem(drawable, instance, phase, program, material, geometry, depth)
    isChanged = true
  }

  private val drawOrder = Comparator<RenderItem> { a, b ->
    if(a.phase != b.phase) a.phase - b.phase
    else if(a.phase == TRANSPARENT && a.depth != b.depth) (if(a.depth > b.depth) -1 else 1)
    else if(a.program != b.program) a.program - b.program
    else if(a.material != b.material) a.material - b.material
    else if(a.geometry != b.geometry) a.geometry - b.geometry
    else a.depth.compareTo(b.depth)
  }

  override fun bufferFor(name : String, pass : Pass) : BufferResource =
    resource ?: throw Error("RenderQueue '${this.name}' has no data yet. Its array is filled when the queue is drawn.")

  private fun upload(pass : Pass) {
    if(items.size > instances.length) {
      var length = instances.length
      while(length < items.size) length *= 2
      instances = Uint32Array(length)
      resource?.buffer?.destroy()
      resource = null
    }
    for(i in 0 until items.size) {
      instances[i] = items[i].instance
    }
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to name,
        "size" to instances.byteLength,
        "usage" to (GPUBufferUsage.STORAGE or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    pass.device.queue.writeBuffer(res.buffer, 0, instances.buffer, 0, maxOf(items.size, 1) * 4)
  }

  /** Sorts the items, then draws every run of the same drawable in one instanced draw call. */
  fun draw(pass : Pass, vararg uniformProviders : UniformProvider) {
    if(isChanged) {
      if(drawnFrame == pass.frame) {
        // the buffer is written when the frame is submitted: the earlier pass would get the new contents, too
        throw Error("RenderQueue '$name' was changed after it was drawn in this frame. Use a separate queue for every pass.")
      }
      items.sortWith(drawOrder)
      upload(pass)
      isChanged = false
    }
    drawnFrame = pass.frame

    batchCount = 0
    var first = 0
    while(first < items.size) {
      val item = items[first]
      var end = first + 1
      while(end < items.size && items[end].drawable === item.drawable && items[end].phase == item.phase) end++
      item.drawable.draw(pass, first, end - first, this, *uniformProviders)
      batchCount++
      first = end
    }
  }
}
