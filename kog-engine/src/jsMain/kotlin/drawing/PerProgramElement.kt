package vision.gears.engine

import vision.gears.webgpumath.*

/**
 * A UniformProvider whose struct `arrays.name` is declared by its own shaders, and that
 * lives in its program's array of that struct: e.g. a material, in the `materials`
 * array of its shaders, which declare their own Material struct.
 *
 *     class Material(arrays : PerProgramArrays, vs : Shader, fs : Shader) :
 *       PerProgramElement(arrays, ShaderProgram.of(...), "material")
 *
 * Its uniforms are gathered from the program on construction. Properties a subclass
 * declares with `by Vec3()` etc. come after, so they are views into the array too.
 * The shaders read the element as `materials[slot]`: e.g. the slot is passed in the
 * instance struct of a RenderQueue.
 */
open class PerProgramElement(
    val arrays : PerProgramArrays,
    val program : ShaderProgram,
    vararg variableNames : String) : UniformProvider(*variableNames, arrays.name) {

  /** Its index in its program's array; 0 if the program has no such array. */
  val slot : Int

  init {
    // textures are not gathered: a material keeps them in its TextureBindings
    program.gatherUniforms(this, withTextures = false)
    slot = if(arrays.isIn(program)) arrays.add(program, this) else 0
  }

  // The array is bound, not the element: it is one buffer for all elements of the program.
  override fun bufferFor(name : String, pass : Pass) : BufferResource =
    if(name == arrays.name) arrays.arrayFor(program).bufferFor(name, pass)
    else super.bufferFor(name, pass)
}
