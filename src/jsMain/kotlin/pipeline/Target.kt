package vision.gears.webgpumath

/**
 * What a render pipeline needs to know of the attachments of a pass: their formats,
 * and the number of samples per pixel.
 *
 * Equal targets are one object: get them with `Target.of`.
 */
class Target private constructor(
  colorFormats : Array<String>,
  val depthFormat : String?,
  /** Samples per pixel of the targets: 1, or 4 for multisampling. All attachments of the pass have as many. */
  val sampleCount : Int,
  /** The same for equal targets, different for different ones. */
  val key : String) {

  /** The formats of the color attachments, in order. */
  val colorFormats : List<String> = colorFormats.toList()

  /** Unique among targets. */
  val id = nextResourceId()

  companion object {
    private val pool = HashMap<String, Target>()

    fun keyOf(colorFormats : Array<String>, depthFormat : String?, sampleCount : Int) : String =
      colorFormats.joinToString(",") + "/" + depthFormat + "/" + sampleCount

    /** The target with these properties: an existing one, or a new one. */
    fun of(colorFormats : Array<String>, depthFormat : String?, sampleCount : Int = 1) : Target {
      val key = keyOf(colorFormats, depthFormat, sampleCount)
      return pool.getOrPut(key) { Target(colorFormats, depthFormat, sampleCount, key) }
    }
  }
}
