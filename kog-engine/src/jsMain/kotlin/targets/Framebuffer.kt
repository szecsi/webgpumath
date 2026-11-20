package vision.gears.engine

import vision.gears.webgpumath.*
import kotlin.js.Json
import kotlin.js.json

/**
 * The textures a pass draws into: color targets, and a depth buffer if `depthFormat` is given.
 *
 *     val pickTarget = Framebuffer(device, arrayOf("rgba32uint"), "depth24plus")
 *     pickTarget.resize(width, height)                  // when the canvas is resized
 *     val pass = pickTarget.begin(encoder, frameCount)   // clears the targets
 *     ... draw ...
 *     pass.end()
 *
 * `targets` are what was drawn: a material can show them, PixelReadback can read them.
 *
 * With `sampleCount` 4, it is drawn with four samples per pixel, into textures of its own;
 * when the pass ends, the samples of each pixel are averaged (resolved) into `targets`.
 * The depth buffer has as many samples. Setting `sampleCount` makes the textures again.
 */
open class Framebuffer protected constructor(
  val device : GPUDevice,
  colorFormats : Array<String>,
  val depthFormat : String?,
  sampleCount : Int,
  width : Int,
  height : Int,
  // false for the canvas: its texture is given anew every frame
  hasOwnTargets : Boolean ) {

  constructor(
    device : GPUDevice,
    colorFormats : Array<String>,
    depthFormat : String? = null,
    sampleCount : Int = 1,
    width : Int = 1,
    height : Int = 1
  ) : this(device, colorFormats, depthFormat, sampleCount, width, height, true)

  val colorFormats : List<String> = colorFormats.toList()

  var width = maxOf(width, 1)
    private set
  var height = maxOf(height, 1)
    private set

  /** Samples per pixel: 1, or 4 for multisampling. Setting it makes the textures again. */
  var sampleCount = sampleCount
    set(value) {
      if(value == field) return
      field = value
      createSampled()
    }

  /**
   * The value the color targets are cleared to, a GPUColor: json("r" to ..., "g" to ..., "b" to ..., "a" to ...).
   * For an integer format, as rgba32uint, the values are integers: they are written as they are.
   */
  var clearColor : Json = json("r" to 0.0, "g" to 0.0, "b" to 0.0, "a" to 1.0)
  /** The value the depth buffer is cleared to: 1, the farthest. */
  var clearDepth = 1.0
  /** The value the stencil buffer is cleared to, if the depth format has one. */
  var clearStencil = 0

  /** The color targets, one sample per pixel, in the order of `colorFormats`. */
  val targets : List<RenderTargetTexture> =
    if(hasOwnTargets) this.colorFormats.map { RenderTargetTexture(device, it, this.width, this.height) }
    else emptyList()

  // With multisampling: the textures that are drawn into, resolved into `targets`.
  private var sampledTargets : List<RenderTargetTexture> = emptyList()

  /** The depth buffer, with `sampleCount` samples per pixel. */
  var depth : RenderTargetTexture? = null
    private set

  init {
    createSampled()
  }

  // the textures whose sample count is `sampleCount`
  private fun createSampled() {
    sampledTargets.forEach { it.destroy() }
    sampledTargets =
      if(sampleCount > 1) colorFormats.map { RenderTargetTexture(device, it, width, height, sampleCount, "$it x$sampleCount") }
      else emptyList()
    depth?.destroy()
    depth = depthFormat?.let { RenderTargetTexture(device, it, width, height, sampleCount) }
  }

  /** Makes the textures again in the new size, if it differs. */
  open fun resize(width : Int, height : Int) {
    val w = maxOf(width, 1)
    val h = maxOf(height, 1)
    if(w == this.width && h == this.height) return
    this.width = w
    this.height = h
    targets.forEach { it.resize(w, h) }
    sampledTargets.forEach { it.resize(w, h) }
    depth?.resize(w, h)
  }

  /** The views the color attachments resolve into, or are drawn into without multisampling. */
  protected open fun targetViews() : List<GPUTextureView> = targets.map { it.view }

  /**
   * Starts a pass that draws into the targets, after clearing them.
   * `frame` is the frame counter: uniform buffers are uploaded once per frame.
   */
  fun begin(encoder : GPUCommandEncoder, frame : Int) : Pass {
    val views = targetViews()
    val colorAttachments = views.indices.map { i ->
      val attachment = json(
        "clearValue" to clearColor,
        "loadOp" to "clear")
      if(sampleCount > 1) {
        // draw into the multisampled texture; its average goes into the target, the samples are not kept
        attachment["view"] = sampledTargets[i].view
        attachment["resolveTarget"] = views[i]
        attachment["storeOp"] = "discard"
      } else {
        attachment["view"] = views[i]
        attachment["storeOp"] = "store"
      }
      attachment
    }.toTypedArray()
    val descriptor = json("colorAttachments" to colorAttachments)
    depth?.let { depth ->
      val attachment = json(
        "view" to depth.view,
        "depthClearValue" to clearDepth,
        "depthLoadOp" to "clear",
        "depthStoreOp" to "store")
      if(depth.hasStencil) {
        attachment["stencilClearValue"] = clearStencil
        attachment["stencilLoadOp"] = "clear"
        attachment["stencilStoreOp"] = "store"
      }
      descriptor["depthStencilAttachment"] = attachment
    }
    // the pipelines are made for these formats and this sample count: the same as the textures above
    return Pass(device, encoder.beginRenderPass(descriptor),
      Target.of(colorFormats.toTypedArray(), depthFormat, sampleCount), frame)
  }
}
