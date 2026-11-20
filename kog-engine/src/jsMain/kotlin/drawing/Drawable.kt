package vision.gears.engine

import vision.gears.webgpumath.*

/**
 * Something that can be drawn with one draw call: typically a mesh,
 * which is a geometry and what it is drawn with.
 *
 * In the vertex shader, `@builtin(instance_index)` takes the values
 * `firstInstance` until `firstInstance + instanceCount - 1`. Resources come from
 * the uniform providers, each for the WGSL variables it claims.
 */
interface Drawable {
  fun draw(pass : Pass, firstInstance : Int, instanceCount : Int, vararg uniformProviders : UniformProvider)
}
