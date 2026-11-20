package vision.gears.engine

import org.w3c.dom.HTMLCanvasElement
import vision.gears.webgpumath.*
import kotlin.js.json

/**
 * The canvas, as what a pass draws into: the picture that appears on the page.
 *
 *     val screen = CanvasFramebuffer(canvas, device, "depth24plus", sampleCount = 4)
 *     screen.resize()                               // after the canvas was resized
 *     val pass = screen.begin(encoder, frameCount)  // clears the canvas and the depth buffer
 *     ... draw ...
 *     pass.end()
 *
 * Making it sets the canvas up for WebGPU drawing. The canvas gives a new texture every
 * frame; `begin` takes the one of the frame. The depth buffer and, with multisampling,
 * the texture the samples are drawn into before they are averaged into the canvas, are ours.
 */
class CanvasFramebuffer(
  val canvas : HTMLCanvasElement,
  device : GPUDevice,
  depthFormat : String? = null,
  sampleCount : Int = 1,
  /** "opaque": the page is not seen through the canvas. "premultiplied": it is, where alpha is below 1. */
  alphaMode : String = "opaque"
) : Framebuffer(device, arrayOf(preferredFormat()), depthFormat, sampleCount,
    canvas.width, canvas.height, hasOwnTargets = false) {

  companion object {
    private fun preferredFormat() : String =
      gpu?.getPreferredCanvasFormat() ?: throw Error("Browser does not support WebGPU")
  }

  val context = canvas.getContext("webgpu").unsafeCast<GPUCanvasContext?>()
    ?: throw Error("Browser does not support WebGPU")

  /** The format of the canvas's textures: what the browser shows the fastest. */
  val format : String get() = colorFormats[0]

  init {
    context.configure(json(
      "device" to device,
      "format" to format,
      "alphaMode" to alphaMode))
  }

  /** Makes the depth buffer and the multisampled texture again, in the size of the canvas. */
  fun resize() = resize(canvas.width, canvas.height)

  override fun targetViews() : List<GPUTextureView> = listOf(context.getCurrentTexture().createView())

  /**
   * A pass that draws over what is already in the canvas, with no depth buffer and one
   * sample per pixel: e.g. for a GUI drawn over the finished picture. Call it after the
   * passes of `begin` in the frame have ended.
   */
  fun beginOverlay(encoder : GPUCommandEncoder, frame : Int) : Pass =
    Pass(device,
      encoder.beginRenderPass(json(
        "colorAttachments" to arrayOf(json(
          "view" to context.getCurrentTexture().createView(),
          "loadOp" to "load",
          "storeOp" to "store")))),
      Target.of(arrayOf(format), null, 1),
      frame)
}
