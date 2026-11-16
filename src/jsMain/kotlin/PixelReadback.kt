package vision.gears.webgpumath

import org.khronos.webgl.Float32Array
import kotlin.js.json

/**
 * Reads one pixel of a texture back from the GPU. The texture must have a format
 * of four 32-bit floats per pixel (rgba32float) and the usage COPY_SRC.
 *
 * WebGPU cannot read a pixel at once, as readPixels of WebGL did. The pixel is
 * copied into a buffer, and the buffer is handed over some time after the frame
 * was submitted. So every frame:
 *
 *     readback.copy(encoder, texture, x, y)   // after the pass that draws into the texture has ended
 *     device.queue.submit(...)
 *     readback.read()                         // after submit
 *
 * and `latest` is the newest pixel that has arrived: usually from one or two frames ago.
 */
class PixelReadback(val device : GPUDevice, val poolSize : Int = 3) {
  /** The newest result: the four values of the pixel. Null until the first one arrives. */
  var latest : Float32Array? = null
    private set

  // A buffer cannot be copied into while it is waiting to be read: several take turns.
  private val freeBuffers = ArrayList<GPUBuffer>()
  private var bufferCount = 0
  private var copied : GPUBuffer? = null
  private var requestCount = 0
  private var latestRequest = -1

  /**
   * Records the copy of the pixel at (x, y), counted from the top left.
   * Returns false, and does nothing, if all buffers are still waiting for earlier pixels.
   */
  fun copy(encoder : GPUCommandEncoder, texture : GPUTexture, x : Int, y : Int) : Boolean {
    if(copied != null) {
      throw Error("PixelReadback: copy was called twice without read. Call read after the frame is submitted.")
    }
    val buffer =
      if(freeBuffers.isNotEmpty()) {
        freeBuffers.removeAt(freeBuffers.size - 1)
      } else if(bufferCount < poolSize) {
        bufferCount++
        device.createBuffer(json(
          "label" to "pixel readback",
          "size" to 16,
          "usage" to (GPUBufferUsage.MAP_READ or GPUBufferUsage.COPY_DST)))
      } else {
        return false
      }
    encoder.copyTextureToBuffer(
      json("texture" to texture, "origin" to arrayOf(
        x.coerceIn(0, texture.width - 1),
        y.coerceIn(0, texture.height - 1),
        0)),
      json("buffer" to buffer),
      arrayOf(1, 1, 1))
    copied = buffer
    return true
  }

  /** Asks for the pixel copied in this frame. Call it after the frame's commands are submitted. */
  fun read() {
    val buffer = copied ?: return
    copied = null
    val request = requestCount++
    buffer.mapAsync(GPUMapMode.READ).then {
      if(request > latestRequest) {
        latestRequest = request
        // the mapped memory is gone after unmap: keep a copy
        latest = Float32Array(buffer.getMappedRange().slice(0))
      }
      buffer.unmap()
      freeBuffers += buffer
    }.catch {
      // e.g. the device was lost: the buffer is not used again
      bufferCount--
    }
  }
}
