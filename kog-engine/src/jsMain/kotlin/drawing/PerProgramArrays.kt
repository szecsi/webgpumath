package vision.gears.engine

import vision.gears.webgpumath.*

/**
 * The arrays called `name` of several shaders, one per ShaderProgram. Each pair of
 * shaders may declare a different struct for it (e.g. each has its own Material struct),
 * so elements with the same shaders share an array, and others get another one.
 * A PerProgramElement puts itself into the array of its program.
 *
 *     val materialArrays = PerProgramArrays("materials")
 *     materialArrays.add(program, material)                        // returns its index in the array
 *     materialArrays.arrayFor(program).bufferFor("materials", pass)
 */
class PerProgramArrays(val name : String, val capacity : Int = 0) {
  private val arrays = HashMap<ShaderProgram, StructuredBuffer>()

  /** Whether the shaders of `program` have an array called `name`. */
  fun isIn(program : ShaderProgram) : Boolean = program.declarations[name] != null

  /** The array of `program`: made on first use, from the program's declaration. */
  fun arrayFor(program : ShaderProgram) : StructuredBuffer =
    arrays.getOrPut(program) {
      val binding = program.declarations[name]
        ?: throw Error("PerProgramArrays '$name': the shaders ${program.debugName} have no '$name' array.")
      StructuredBuffer(name, binding, capacity)
    }

  /** Puts `element` into the array of `program`. Returns its index there. */
  fun add(program : ShaderProgram, element : UniformProvider) : Int = arrayFor(program).add(element)

  /** Takes `element` out of the array of `program`; the last element moves into its place. */
  fun remove(program : ShaderProgram, element : UniformProvider) = arrayFor(program).remove(element)
}
