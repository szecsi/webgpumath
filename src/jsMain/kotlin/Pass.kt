package vision.gears.webgpumath

/**
 * Fixed-function state of a draw call: what WebGL set with gl.enable, gl.depthFunc,
 * gl.blendFunc. In WebGPU it is part of the pipeline.
 */
class RenderState(
  var depthCompare : String = "less",
  var depthWrite : Boolean = true,
  var cullMode : String = "none",
  /** A GPUBlendState object, or null for no blending. */
  var blend : dynamic = null ) {
  val key get() = "$depthCompare/$depthWrite/$cullMode/${if(blend == null) "" else JSON.stringify(blend)}"

  companion object {
    val default = RenderState()
  }
}

/**
 * A render pass being recorded, with what drawing into it needs to know:
 * the formats of its targets, and the frame it belongs to.
 */
class Pass(
  val device : GPUDevice,
  val encoder : GPURenderPassEncoder,
  val colorFormats : Array<String>,
  val depthFormat : String?,
  /** Frame counter. Uniform buffers are uploaded once per frame. */
  val frame : Int ) {

  val targetKey = colorFormats.joinToString(",") + "/" + depthFormat
}
