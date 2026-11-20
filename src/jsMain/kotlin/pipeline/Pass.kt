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

  /** Draws only into this rectangle of the targets, in pixels from the top left. */
  fun scissor(x : Int, y : Int, width : Int, height : Int) = encoder.setScissorRect(x, y, width, height)

  /** Ends the pass: nothing more can be drawn into it. */
  fun end() = encoder.end()
}
