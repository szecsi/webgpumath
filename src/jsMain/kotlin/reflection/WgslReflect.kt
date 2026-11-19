@file:JsModule("wgsl_reflect")
@file:JsNonModule

package vision.gears.webgpumath

/**
 * The reflection class of the wgsl_reflect npm package. Its result objects are read
 * dynamically and converted to Kotlin classes in ShaderReflection.
 */
external class WgslReflect(code : String)
