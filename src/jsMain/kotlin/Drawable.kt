package vision.gears.webgpumath

abstract class Drawable {
  /** Unique among drawables. A RenderQueue sorts by the ids of programs, materials and geometries. */
  val id = nextResourceId()

  open fun gatherUniforms(target : UniformProvider){}
  abstract fun draw(pass : Pass, vararg uniformProviders : UniformProvider)
  open fun drawWithOverrides(pass : Pass, overrides : Map<String, UniformProvider>, vararg uniformProviders : UniformProvider){
    draw(pass, *uniformProviders)
  }
}
