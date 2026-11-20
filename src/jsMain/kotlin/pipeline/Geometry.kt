package vision.gears.webgpumath

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import kotlin.js.json

/** One vertex attribute in its own buffer. `format` is a GPUVertexFormat such as "float32x3". */
class VertexAttribute(val buffer : GPUBuffer, val format : String)

/**
 * Vertex and index buffers. Attributes are matched by name to the inputs of the
 * vertex shader, e.g. `@location(0) vertexPosition : vec4f`. A geometry may have
 * attributes the shader does not use.
 *
 * `count` is the number of indices if there is an index buffer, the number of vertices otherwise.
 *
 * `layout` is what the pipeline needs to know of the geometry. `draw` makes the draw
 * call. The pipeline (`program.pipeline(pass, geometry.layout, renderState)`) must be
 * set and the resources of the shaders bound (`program.bind`) before.
 */
open class Geometry(
    vertexAttributes : Map<String, VertexAttribute>,
    val indexBuffer : GPUBuffer? = null,
    val indexFormat : String = "uint16",
    val count : Int,
    topology : String = "triangle-list") {

  /** Unique among geometries. A RenderQueue can sort by it. */
  val id = nextResourceId()

  val vertexAttributes : Map<String, VertexAttribute> = HashMap(vertexAttributes)

  val layout = VertexLayout.of(
    vertexAttributes.mapValues { it.value.format },
    topology,
    if(indexBuffer != null && topology.endsWith("-strip")) indexFormat else null)

  /**
   * Sets the vertex buffers that the vertex shader of `program` reads, and makes the draw call.
   * `@builtin(instance_index)` goes from `firstInstance` to `firstInstance + instanceCount - 1`.
   */
  fun draw(pass : Pass, program : ShaderProgram, firstInstance : Int = 0, instanceCount : Int = 1) {
    val encoder = pass.encoder
    program.vertexInputs.forEachIndexed { slot, input ->
      encoder.setVertexBuffer(slot, vertexAttributes[input.name]!!.buffer)
    }
    val indices = indexBuffer
    if(indices != null) {
      encoder.setIndexBuffer(indices, indexFormat)
      encoder.drawIndexed(count, instanceCount, 0, 0, firstInstance)
    } else {
      encoder.draw(count, instanceCount, 0, firstInstance)
    }
  }

  companion object {
    /** Creates a GPU buffer holding a copy of `data`. `usage` is GPUBufferUsage.VERTEX or INDEX. */
    fun createBuffer(device : GPUDevice, data : ArrayBufferView, usage : Int) : GPUBuffer {
      val buffer = device.createBuffer(json(
        "size" to (data.byteLength + 3) / 4 * 4,
        "usage" to usage,
        "mappedAtCreation" to true))
      Uint8Array(buffer.getMappedRange()).set(Uint8Array(data.buffer, data.byteOffset, data.byteLength))
      buffer.unmap()
      return buffer
    }
  }
}
