package vision.gears.engine

import org.w3c.dom.Image
import vision.gears.webgpumath.*
import kotlin.js.json

class TextureCube(
  device : GPUDevice,
  vararg mediaFileUrls : String // +x, -x, +y, -y, +z, -z
  ) : Texture {

  // a white cube stands in until the images have loaded
  override var view : GPUTextureView = TextureTools.placeholder(device, "cube").view
    private set
  override val sampler : GPUSampler = device.createSampler(json(
    "magFilter" to "linear",
    "minFilter" to "linear",
    "mipmapFilter" to "linear"))
  override var resourceId = nextResourceId()
    private set
  // the shader declares it as texture_cube<f32>
  override val viewDimension = "cube"

  init {
    val images = Array<Image>(6) { Image() }
    var loadedCount = 0
    for(i in 0 until 6) {
      images[i].onload = {
        if(++loadedCount == 6) {
          val width = images[0].width
          val height = images[0].height
          val gpuTexture = device.createTexture(json(
            "label" to mediaFileUrls[0],
            "size" to arrayOf(width, height, 6), // six layers: the faces
            "format" to "rgba8unorm",
            "mipLevelCount" to TextureTools.mipLevelCount(width, height),
            "usage" to (GPUTextureUsage.TEXTURE_BINDING or GPUTextureUsage.COPY_DST or GPUTextureUsage.RENDER_ATTACHMENT)))
          for(face in 0 until 6) {
            device.queue.copyExternalImageToTexture(
              json("source" to images[face]),
              json("texture" to gpuTexture, "origin" to arrayOf(0, 0, face)),
              arrayOf(width, height))
          }
          TextureTools.generateMipmaps(device, gpuTexture)
          view = gpuTexture.createView(json("dimension" to "cube"))
          resourceId = nextResourceId() // tells bind groups that the view has changed
        }
      }
      images[i].src = mediaFileUrls[i]
    }
  }
}
