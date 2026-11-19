package vision.gears.webgpumath

/**
 * A render pass being recorded, with what drawing into it needs to know:
 * its target, and the frame it belongs to.
 */
class Pass(
  val device : GPUDevice,
  val encoder : GPURenderPassEncoder,
  /** The formats and the sample count of the attachments. */
  val target : Target,
  /** Frame counter. Uniform buffers are uploaded once per frame. */
  val frame : Int ) {
}
