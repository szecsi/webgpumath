package vision.gears.webgpumath

import org.khronos.webgl.Uint8Array
import kotlin.js.json
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.max

/** Helpers for what WebGL did for textures by itself: mipmap generation and unset texture units. */
object TextureTools {

  /** Number of mip levels of a full chain down to 1x1. */
  fun mipLevelCount(width : Int, height : Int) : Int = floor(log2(max(width, height).toDouble())).toInt() + 1

  private const val mipShaderSource = """
struct VsOut { @builtin(position) position : vec4f, @location(0) uv : vec2f }
@group(0) @binding(0) var mipSampler : sampler;
@group(0) @binding(1) var mipSource : texture_2d<f32>;
@vertex fn vsMain(@builtin(vertex_index) i : u32) -> VsOut {
  // one triangle covering the viewport
  let p = vec2f(f32((i << 1u) & 2u), f32(i & 2u));
  var o : VsOut;
  o.position = vec4f(p * 2.0 - 1.0, 0.0, 1.0);
  o.uv = vec2f(p.x, 1.0 - p.y);
  return o;
}
@fragment fn fsMain(input : VsOut) -> @location(0) vec4f {
  return textureSample(mipSource, mipSampler, input.uv);
}
"""
  private var mipModule : GPUShaderModule? = null
  private var mipSampler : GPUSampler? = null
  private val mipPipelines = HashMap<String, GPURenderPipeline>()

  /**
   * Fills mip levels 1.. of every layer of `texture` by filtering the level above.
   * The texture must have been created with RENDER_ATTACHMENT and TEXTURE_BINDING usage.
   */
  fun generateMipmaps(device : GPUDevice, texture : GPUTexture) {
    if(texture.mipLevelCount < 2) return
    val module = mipModule ?: device.createShaderModule(json("code" to mipShaderSource)).also { mipModule = it }
    val sampler = mipSampler ?: device.createSampler(json("minFilter" to "linear", "magFilter" to "linear")).also { mipSampler = it }
    val pipeline = mipPipelines.getOrPut(texture.format) {
      device.createRenderPipeline(json(
        "layout" to "auto",
        "vertex" to json("module" to module, "entryPoint" to "vsMain"),
        "fragment" to json("module" to module, "entryPoint" to "fsMain",
          "targets" to arrayOf(json("format" to texture.format))),
        "primitive" to json("topology" to "triangle-list")))
    }
    val encoder = device.createCommandEncoder()
    for(layer in 0 until texture.depthOrArrayLayers) {
      for(level in 1 until texture.mipLevelCount) {
        fun levelView(l : Int) = texture.createView(json(
          "dimension" to "2d",
          "baseMipLevel" to l, "mipLevelCount" to 1,
          "baseArrayLayer" to layer, "arrayLayerCount" to 1))
        val bindGroup = device.createBindGroup(json(
          "layout" to pipeline.getBindGroupLayout(0),
          "entries" to arrayOf(
            json("binding" to 0, "resource" to sampler),
            json("binding" to 1, "resource" to levelView(level - 1)))))
        val pass = encoder.beginRenderPass(json(
          "colorAttachments" to arrayOf(json(
            "view" to levelView(level),
            "loadOp" to "clear",
            "storeOp" to "store"))))
        pass.setPipeline(pipeline)
        pass.setBindGroup(0, bindGroup)
        pass.draw(3)
        pass.end()
      }
    }
    device.queue.submit(arrayOf(encoder.finish()))
  }

  private val placeholders = HashMap<String, Texture>()

  /**
   * A 1x1 white texture of the given view dimension ("2d", "cube", "3d", "2d-array").
   * Bound where no texture was set, and usable as a stand-in while an image is loading.
   */
  fun placeholder(device : GPUDevice, viewDimension : String = "2d") : Texture =
    placeholders.getOrPut(viewDimension) {
      val layerCount = if(viewDimension == "cube") 6 else 1
      val gpuTexture = device.createTexture(json(
        "size" to arrayOf(1, 1, layerCount),
        "dimension" to if(viewDimension == "3d") "3d" else "2d",
        "format" to "rgba8unorm",
        "usage" to (GPUTextureUsage.TEXTURE_BINDING or GPUTextureUsage.COPY_DST)))
      val pixels = Uint8Array(4 * layerCount)
      pixels.asDynamic().fill(255)
      device.queue.writeTexture(
        json("texture" to gpuTexture),
        pixels,
        json("bytesPerRow" to 4, "rowsPerImage" to 1),
        arrayOf(1, 1, layerCount))
      val dimension = viewDimension
      object : Texture {
        override val view = gpuTexture.createView(json("dimension" to dimension))
        override val sampler = device.createSampler()
        override val resourceId = nextResourceId()
        override val viewDimension = dimension
      }
    }
}
