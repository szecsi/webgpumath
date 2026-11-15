package vision.gears.webgpumath

import org.khronos.webgl.ArrayBufferView
import org.khronos.webgl.Uint8Array
import kotlin.js.json

/** One vertex attribute in its own buffer. `format` is a GPUVertexFormat such as "float32x3". */
class VertexAttribute(val buffer : GPUBuffer, val format : String) {
  val stride : Int
  init {
    val componentCount = format.substringAfter('x', "1").toInt()
    val componentType = format.substringBefore('x')
    val componentSize = when {
      componentType.endsWith("32") -> 4
      componentType.endsWith("16") || componentType.endsWith("16norm") || componentType.endsWith("16float") -> 2
      else -> 1
    }
    stride = componentCount * componentSize
  }
}

/**
 * Vertex and index buffers. Attributes are matched by name to the inputs of the
 * vertex shader, e.g. `@location(0) vertexPosition : vec4f`. A geometry may have
 * attributes the shader does not use.
 *
 * As the last link of the draw chain, it makes the draw call with the program
 * and the instance range that were left in the pass.
 */
abstract class Geometry : Drawable() {
  val vertexAttributes = HashMap<String, VertexAttribute>()
  var topology = "triangle-list"
  var indexBuffer : GPUBuffer? = null
  var indexFormat = "uint16"
  /** Number of indices if there is an index buffer, number of vertices otherwise. */
  var count = 0

  val layoutKey get() = vertexAttributes.entries.sortedBy { it.key }.joinToString(",", postfix = "/$topology") { "${it.key}:${it.value.format}" }

  final override fun draw(pass : Pass, vararg uniformProviders : UniformProvider){
    val program = pass.program ?: throw Error("${this::class.simpleName} drawn without a program. A geometry must come after its material in the mesh.")
    val encoder = pass.encoder
    encoder.setPipeline(program.pipeline(pass, this))
    program.vertexInputs.forEachIndexed { slot, input ->
      encoder.setVertexBuffer(slot, vertexAttributes[input.name]!!.buffer)
    }
    val indices = indexBuffer
    if(indices != null) {
      encoder.setIndexBuffer(indices, indexFormat)
      encoder.drawIndexed(count, pass.instanceCount, 0, 0, pass.firstInstance)
    } else {
      encoder.draw(count, pass.instanceCount, 0, pass.firstInstance)
    }
  }

  final override fun drawWithOverrides(pass : Pass, overrides : Map<String, UniformProvider>, vararg uniformProviders : UniformProvider) {
    draw(pass, *uniformProviders)
  }

  final override fun gatherUniforms(target : UniformProvider){}

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
