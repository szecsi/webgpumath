package vision.gears.webgpumath

import kotlinx.browser.window
import org.khronos.webgl.ArrayBuffer
import kotlin.js.Promise

// External declarations for the part of the WebGPU API that webgpumath and its client
// projects use. Descriptors are plain JavaScript objects, built with kotlin.js.json(...),
// so client code reads like the WebGPU documentation. Extend this file as needed.

/** `navigator.gpu`, or null if the browser does not support WebGPU. */
val gpu : GPU? get() = window.navigator.asDynamic().gpu.unsafeCast<GPU?>()

external interface GPU {
  fun requestAdapter(options : dynamic = definedExternally) : Promise<GPUAdapter?>
  fun getPreferredCanvasFormat() : String
}

external interface GPUAdapter {
  fun requestDevice(descriptor : dynamic = definedExternally) : Promise<GPUDevice>
}

external interface GPUDevice {
  val queue : GPUQueue
  var onuncapturederror : ((event : GPUUncapturedErrorEvent) -> Unit)?
  fun createBuffer(descriptor : dynamic) : GPUBuffer
  fun createTexture(descriptor : dynamic) : GPUTexture
  fun createSampler(descriptor : dynamic = definedExternally) : GPUSampler
  fun createShaderModule(descriptor : dynamic) : GPUShaderModule
  fun createBindGroupLayout(descriptor : dynamic) : GPUBindGroupLayout
  fun createPipelineLayout(descriptor : dynamic) : GPUPipelineLayout
  fun createBindGroup(descriptor : dynamic) : GPUBindGroup
  fun createRenderPipeline(descriptor : dynamic) : GPURenderPipeline
  fun createCommandEncoder(descriptor : dynamic = definedExternally) : GPUCommandEncoder
  fun pushErrorScope(filter : String)
  fun popErrorScope() : Promise<GPUError?>
}

external interface GPUQueue {
  /** `data` is an ArrayBuffer (offset and size in bytes) or a typed array (in elements). */
  fun writeBuffer(buffer : GPUBuffer, bufferOffset : Int, data : dynamic, dataOffset : Int = definedExternally, size : Int = definedExternally)
  fun writeTexture(destination : dynamic, data : dynamic, dataLayout : dynamic, size : dynamic)
  fun copyExternalImageToTexture(source : dynamic, destination : dynamic, copySize : dynamic)
  fun submit(commandBuffers : Array<GPUCommandBuffer>)
}

external interface GPUBuffer {
  val size : Double
  /** `mode` is a GPUMapMode. The buffer can be read or written when the promise is fulfilled, until `unmap`. */
  fun mapAsync(mode : Int, offset : Int = definedExternally, size : Int = definedExternally) : Promise<Unit>
  fun getMappedRange(offset : Int = definedExternally, size : Int = definedExternally) : ArrayBuffer
  fun unmap()
  fun destroy()
}

external interface GPUTexture {
  val width : Int
  val height : Int
  val depthOrArrayLayers : Int
  val mipLevelCount : Int
  val format : String
  fun createView(descriptor : dynamic = definedExternally) : GPUTextureView
  fun destroy()
}

external interface GPUTextureView
external interface GPUSampler
external interface GPUBindGroupLayout
external interface GPUPipelineLayout
external interface GPUBindGroup
external interface GPUCommandBuffer

external interface GPUShaderModule {
  fun getCompilationInfo() : Promise<GPUCompilationInfo>
}

external interface GPUCompilationInfo {
  val messages : Array<GPUCompilationMessage>
}

external interface GPUCompilationMessage {
  val message : String
  /** "error", "warning" or "info" */
  val type : String
  val lineNum : Int
  val linePos : Int
}

external interface GPUError {
  val message : String
}

external interface GPUUncapturedErrorEvent {
  val error : GPUError
}

external interface GPURenderPipeline {
  fun getBindGroupLayout(index : Int) : GPUBindGroupLayout
}

external interface GPUCommandEncoder {
  fun beginRenderPass(descriptor : dynamic) : GPURenderPassEncoder
  fun copyTextureToBuffer(source : dynamic, destination : dynamic, copySize : dynamic)
  fun finish() : GPUCommandBuffer
}

external interface GPURenderPassEncoder {
  fun setPipeline(pipeline : GPURenderPipeline)
  fun setBindGroup(index : Int, bindGroup : GPUBindGroup)
  fun setVertexBuffer(slot : Int, buffer : GPUBuffer)
  fun setIndexBuffer(buffer : GPUBuffer, indexFormat : String)
  fun setViewport(x : Float, y : Float, width : Float, height : Float, minDepth : Float, maxDepth : Float)
  /** Only pixels in the rectangle are drawn to. */
  fun setScissorRect(x : Int, y : Int, width : Int, height : Int)
  fun draw(vertexCount : Int, instanceCount : Int = definedExternally, firstVertex : Int = definedExternally, firstInstance : Int = definedExternally)
  fun drawIndexed(indexCount : Int, instanceCount : Int = definedExternally, firstIndex : Int = definedExternally, baseVertex : Int = definedExternally, firstInstance : Int = definedExternally)
  fun end()
}

external interface GPUCanvasContext {
  fun configure(configuration : dynamic)
  fun getCurrentTexture() : GPUTexture
}

external object GPUBufferUsage {
  val MAP_READ : Int
  val MAP_WRITE : Int
  val COPY_SRC : Int
  val COPY_DST : Int
  val INDEX : Int
  val VERTEX : Int
  val UNIFORM : Int
  val STORAGE : Int
}

external object GPUMapMode {
  val READ : Int
  val WRITE : Int
}

external object GPUTextureUsage {
  val COPY_SRC : Int
  val COPY_DST : Int
  val TEXTURE_BINDING : Int
  val STORAGE_BINDING : Int
  val RENDER_ATTACHMENT : Int
}

external object GPUShaderStage {
  val VERTEX : Int
  val FRAGMENT : Int
  val COMPUTE : Int
}

private var resourceIdCounter = 0
/** Unique ids for GPU resources; bind groups are cached by the ids of what they bind. */
fun nextResourceId() : Int = ++resourceIdCounter
