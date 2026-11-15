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
 * A render pass being recorded, with what drawables need to know about it:
 * the formats of its targets, and the frame it belongs to.
 *
 * Drawing is a chain, as in webglmath: the program binds its resources and
 * leaves itself in `program`, then the geometry issues the draw call with the
 * pipeline that fits both.
 */
class Pass(
  val device : GPUDevice,
  val encoder : GPURenderPassEncoder,
  val colorFormats : Array<String>,
  val depthFormat : String?,
  /** Frame counter. Uniform buffers are uploaded once per frame. */
  val frame : Int ) {

  var program : ProgramReflection? = null
  var renderState : RenderState = RenderState.default
  var firstInstance = 0
  var instanceCount = 1

  val targetKey = colorFormats.joinToString(",") + "/" + depthFormat
}
