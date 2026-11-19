package vision.gears.webgpumath

import org.khronos.webgl.Uint32Array
import org.khronos.webgl.set
import kotlin.js.json

/**
 * One thing to draw: `drawable`, with the per-instance values `instance`.
 * The rest is for sorting and batching, and the two states that are set while drawing.
 */
class RenderItem(
  val drawable : Drawable,
  val phase : Int,
  val template : Int,
  val batch : Int,
  val geometry : Int,
  val depth : Float,
  val instance : IntArray,
  val stencilReference : Int?,
  val blendConstant : dynamic )

/**
 * Collects what is to be drawn in a pass, then draws it in an order that needs
 * few state changes, merging neighbours of the same batch and geometry into one
 * instanced draw call.
 *
 * The instances of such a call are consecutive values of `instance_index`. What
 * each instance stands for is in an array of structs of u32 members that the queue
 * claims, declared in the Prelude, e.g.:
 *
 *     struct Instance { object : u32, entity : u32, material : u32 }
 *     @group(2) @binding(1) var<storage, read> instances : array<Instance>;
 *
 * `add` takes the values of the members, in order. The vertex shader reads
 * `instances[instance_index]`, and indexes other arrays with its members.
 *
 * Sorting has several levels:
 * 1. phase: BACKGROUND, OPAQUE, TRANSPARENT, or any other number, in increasing order;
 * 2. in the TRANSPARENT phase, depth, farthest first;
 * 3. pipeline template, batch, geometry ids, so that equal pipelines and bind groups are neighbours;
 * 4. stencil reference, so that items with the same one are neighbours;
 * 5. depth, nearest first, so that the depth test rejects more of what comes later.
 *
 * Neighbours with the same phase, batch, geometry, stencil reference and blend constant
 * are drawn in one call, by the drawable of the first of them. So items of one batch must
 * be drawable with the same pipeline and bind groups: the same shaders, render state and textures.
 *
 * The stencil reference and the blend constant are not in the pipeline: the queue sets
 * them before a draw call, when they differ from what it set before in the pass. An item
 * that gives none leaves them as they are. They are given per item, as they belong to
 * what is drawn this frame: e.g. which of the visible portals a portal surface marks in
 * the stencil buffer, or that the scene is being drawn as seen through portal i.
 * The instances of one draw call cannot differ in them.
 *
 * Every frame: `clear`, `add` everything, then `draw` once. To draw different
 * things in another pass of the same frame, use another queue with its own array.
 */
class RenderQueue(val name : String = "instances") : UniformProvider(name) {
  companion object {
    const val BACKGROUND = 0
    const val OPAQUE = 1
    const val TRANSPARENT = 2
  }

  /** The number of u32 values per instance. */
  val valuesPerInstance : Int

  init {
    val binding = Prelude.binding(name)
      ?: throw Error("RenderQueue '$name': no such variable in the shader prelude. Load the prelude first, and declare '$name' in it as a storage array of a struct of u32 members.")
    val struct = binding.struct
    if(binding.kind != BindingKind.STORAGE || !binding.isArray || struct == null ||
        struct.members.any { it.typeName != "u32" || it.isArray } ||
        binding.arrayStride != struct.members.size * 4) {
      throw Error("RenderQueue '$name': the WGSL variable must be a storage array of a struct of u32 members, but it is ${binding.typeName}.")
    }
    valuesPerInstance = struct.members.size
  }

  private val items = ArrayList<RenderItem>()
  private var instances = Uint32Array(256 * valuesPerInstance)
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
   * `instance` are the values of the members of the instance struct: e.g. where the
   * object's and the material's data are in their arrays. `template`, `batch` and
   * `geometry` are numbers that tell apart what `drawable` consists of: the `id` of a
   * PipelineTemplate and of a geometry, and for the batch, a number that is the same
   * for materials that can be drawn in one call.
   *
   * `stencilReference` is the value the stencil test compares with, and that the
   * "replace" stencil operation writes. `blendConstant` is a GPUColor, e.g.
   * json("r" to 1, "g" to 1, "b" to 1, "a" to 0.5), for the "constant" blend factors.
   * Coming after `instance`, they are given by name.
   */
  fun add(
      drawable : Drawable,
      phase : Int = OPAQUE,
      template : Int = 0,
      batch : Int = 0,
      geometry : Int = 0,
      depth : Float = 0.0f,
      vararg instance : Int,
      stencilReference : Int? = null,
      blendConstant : dynamic = null ) {
    if(instance.size != valuesPerInstance) {
      throw Error("RenderQueue '$name': an instance has $valuesPerInstance values, but ${instance.size} were given.")
    }
    items += RenderItem(drawable, phase, template, batch, geometry, depth, instance, stencilReference, blendConstant)
    isChanged = true
  }

  private val drawOrder = Comparator<RenderItem> { a, b ->
    if(a.phase != b.phase) a.phase - b.phase
    else if(a.phase == TRANSPARENT && a.depth != b.depth) (if(a.depth > b.depth) -1 else 1)
    else if(a.template != b.template) a.template - b.template
    else if(a.batch != b.batch) a.batch - b.batch
    else if(a.geometry != b.geometry) a.geometry - b.geometry
    else if(a.stencilReference != b.stencilReference) (a.stencilReference ?: -1) - (b.stencilReference ?: -1)
    else a.depth.compareTo(b.depth)
  }

  override fun bufferFor(name : String, pass : Pass) : BufferResource =
    resource ?: throw Error("RenderQueue '${this.name}' has no data yet. Its array is filled when the queue is drawn.")

  private fun upload(pass : Pass) {
    val needed = items.size * valuesPerInstance
    if(needed > instances.length) {
      var length = instances.length
      while(length < needed) length *= 2
      instances = Uint32Array(length)
      resource?.buffer?.destroy()
      resource = null
    }
    for(i in 0 until items.size) {
      val values = items[i].instance
      for(k in 0 until valuesPerInstance) {
        instances[i * valuesPerInstance + k] = values[k]
      }
    }
    val res = resource ?: BufferResource(
      pass.device.createBuffer(json(
        "label" to name,
        "size" to instances.byteLength,
        "usage" to (GPUBufferUsage.STORAGE or GPUBufferUsage.COPY_DST))),
      nextResourceId()).also { resource = it }
    pass.device.queue.writeBuffer(res.buffer, 0, instances.buffer, 0, maxOf(items.size, 1) * valuesPerInstance * 4)
  }

  /** Sorts the items, then draws every run of the same batch and geometry in one instanced draw call. */
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
    var stencilReference : Int? = null
    var blendConstant : dynamic = null
    var first = 0
    while(first < items.size) {
      val item = items[first]
      var end = first + 1
      while(end < items.size &&
          items[end].phase == item.phase &&
          items[end].template == item.template &&
          items[end].batch == item.batch &&
          items[end].geometry == item.geometry &&
          items[end].stencilReference == item.stencilReference &&
          items[end].blendConstant === item.blendConstant) end++
      // the states that are not in the pipeline, when they change
      val reference = item.stencilReference
      if(reference != null && reference != stencilReference) {
        pass.encoder.setStencilReference(reference)
        stencilReference = reference
      }
      if(item.blendConstant != null && item.blendConstant !== blendConstant) {
        pass.encoder.setBlendConstant(item.blendConstant)
        blendConstant = item.blendConstant
      }
      item.drawable.draw(pass, first, end - first, this, *uniformProviders)
      batchCount++
      first = end
    }
  }
}
