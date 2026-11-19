package vision.gears.webgpumath

import org.w3c.xhr.XMLHttpRequest

/**
 * The WGSL declarations shared by all shaders of a project: the structs and resource
 * variables of the uniform providers that are not specific to one shader (camera,
 * lights, per-object data). The shader loader appends `source` to every shader.
 *
 * Load it before creating any UniformProvider, StructuredBuffer or StructuredElement: they take their
 * memory layout from here.
 */
object Prelude {
  var source = ""
    private set
  var reflection : ShaderReflection? = null
    private set

  fun set(wgslSource : String, debugName : String = "prelude") {
    source = wgslSource
    reflection = ShaderReflection(wgslSource, debugName)
  }

  /** Loads the prelude synchronously, so that it is available to the constructors that follow. */
  fun load(url : String) {
    val request = XMLHttpRequest()
    request.overrideMimeType("text/plain")
    request.open("GET", url, false)
    request.send()
    if(request.status.toInt() != 200 || request.responseText == "") {
      throw Error("Could not load shader prelude $url.")
    }
    set(request.responseText, url)
  }

  fun binding(name : String) : BindingInfo? = reflection?.bindings?.get(name)
}
