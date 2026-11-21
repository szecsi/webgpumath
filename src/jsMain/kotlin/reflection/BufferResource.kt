package vision.gears.webgpumath

/** A GPU buffer to be bound, with the id that bind group caching uses. */
class BufferResource(val buffer : GPUBuffer, val resourceId : Int)