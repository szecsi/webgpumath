package vision.gears.engine

import org.w3c.dom.Image
import vision.gears.webgpumath.*
import kotlin.js.json

class Texture2D(
  device : GPUDevice,
  val mediaFileUrl : String
  ) : Texture {

  // a white pixel stands in until the image has loaded
  override var view : GPUTextureView = TextureTools.placeholder(device, "2d").view
    private set
  override val sampler : GPUSampler = device.createSampler(json(
    "magFilter" to "linear",
    "minFilter" to "linear",
    "mipmapFilter" to "linear",
    "addressModeU" to "repeat",
    "addressModeV" to "repeat"))
  override var resourceId = nextResourceId()
    private set
  // the shader declares it as texture_2d<f32>
  override val viewDimension = "2d"

  init {
    val image = Image()
    image.onload = {
      val gpuTexture = device.createTexture(json(
        "label" to mediaFileUrl,
        "size" to arrayOf(image.width, image.height),
        "format" to "rgba8unorm",
        "mipLevelCount" to TextureTools.mipLevelCount(image.width, image.height),
        "usage" to (GPUTextureUsage.TEXTURE_BINDING or GPUTextureUsage.COPY_DST or GPUTextureUsage.RENDER_ATTACHMENT)))
      device.queue.copyExternalImageToTexture(
        json("source" to image),
        json("texture" to gpuTexture),
        arrayOf(image.width, image.height))
      TextureTools.generateMipmaps(device, gpuTexture)
      view = gpuTexture.createView()
      resourceId = nextResourceId() // tells bind groups that the view has changed
    }
    image.src = mediaFileUrl
  }
}
