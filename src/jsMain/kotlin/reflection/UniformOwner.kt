package vision.gears.webgpumath

/**
 * Something whose properties declared with `by Vec3()`, `by Mat4()` etc. are uniforms
 * of `provider`. A UniformProvider is its own owner. A class that cannot derive from
 * UniformProvider, as an ECS component, owns one instead and names it:
 *
 *     class ModelMatrix(gameObjects : StructuredBuffer) : Component(), UniformOwner {
 *       override val provider = StructuredElement(gameObjects)   // before the properties
 *       val modelMatrix by Mat4()
 *     }
 */
interface UniformOwner {
  val provider : UniformProvider
}
