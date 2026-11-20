package vision.gears.engine

import vision.gears.webgpumath.*
import kotlin.js.json

/**
 * A texture that is drawn into: a color or depth attachment of a Framebuffer.
 *
 * With one sample per pixel it is also a Texture: a material can show what was drawn
 * into it, e.g. `material.textures = TextureBindings.of("renderedImage" to framebuffer.targets[0])`,
 * and a color one can be copied from, e.g. by PixelReadback.
 *
 * `resize` makes it again, in the new size; what was in it is lost.
 */
class RenderTargetTexture(
  val device : GPUDevice,
  /** The GPUTextureFormat: "rgba8unorm", "rgba32uint", "depth24plus", ... */
  val format : String,
  width : Int = 1,
  height : Int = 1,
  /** Samples per pixel: 1, or 4 for multisampling. */
  val sampleCount : Int = 1,
  val label : String = format ) : Texture {

  var width = maxOf(width, 1)
    private set
  var height = maxOf(height, 1)
    private set

  val isDepth : Boolean get() = format.startsWith("depth") || format == "stencil8"
  val hasStencil : Boolean get() = format.contains("stencil")

  lateinit var texture : GPUTexture
    private set
  override lateinit var view : GPUTextureView
    private set
  override val sampler : GPUSampler = device.createSampler(json(
    "magFilter" to "linear",
    "minFilter" to "linear"))
  override var resourceId = 0
    private set
  override val viewDimension = "2d"

  init {
    create()
  }

  private fun create() {
    // A multisampled texture is only drawn into: it is resolved into one with a sample per pixel.
    var usage = GPUTextureUsage.RENDER_ATTACHMENT
    if(sampleCount == 1) usage = usage or GPUTextureUsage.TEXTURE_BINDING
    if(sampleCount == 1 && !isDepth) usage = usage or GPUTextureUsage.COPY_SRC
    texture = device.createTexture(json(
      "label" to label,
      "size" to arrayOf(width, height),
      "format" to format,
      "sampleCount" to sampleCount,
      "usage" to usage))
    view = texture.createView()
    resourceId = nextResourceId() // tells bind groups that the view has changed
  }

  /** Makes the texture again in the new size, if it differs. */
  fun resize(width : Int, height : Int) {
    val w = maxOf(width, 1)
    val h = maxOf(height, 1)
    if(w == this.width && h == this.height) return
    this.width = w
    this.height = h
    texture.destroy() // freed once the GPU is done with what was already submitted
    create()
  }

  fun destroy() = texture.destroy()
}
