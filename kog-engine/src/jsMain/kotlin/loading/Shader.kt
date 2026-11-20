package vision.gears.engine

import org.w3c.xhr.XMLHttpRequest
import vision.gears.webgpumath.*
import kotlin.js.json

/**
 * A WGSL shader loaded from a file. The stage is given by the entry point
 * in the file: a function marked @vertex or @fragment.
 */
class Shader (
  val device : GPUDevice,
  val sourceUrl : String) {

  val module : GPUShaderModule
  val reflection : ShaderReflection

  init {
    val request = XMLHttpRequest()
    request.overrideMimeType("text/plain")
    request.open("GET", sourceUrl, false)
    request.send()
    val source = request.responseText
    if(request.status.toInt() != 200 || source == ""){
      throw Error("${sourceUrl} is missing or empty.")
    }
    // The shared declarations come after the shader text, so that the line
    // numbers of error messages match the shader file. (Order does not matter in WGSL.)
    val sourceLineCount = source.count { it == '\n' } + 1
    val fullSource = source + "\n" + Prelude.source

    module = device.createShaderModule(json("label" to sourceUrl, "code" to fullSource))
    module.getCompilationInfo().then { info ->
      for(message in info.messages) {
        val line = message.lineNum
        val location =
          if(line <= sourceLineCount) "${request.responseURL}:${line}:${message.linePos}"
          else "shader prelude:${line - sourceLineCount}:${message.linePos}"
        val text = "Shader ${sourceUrl}: ${message.type} at ${location}\n${message.message}"
        if(message.type == "error") console.error(text) else console.warn(text)
      }
    }

    reflection = ShaderReflection(fullSource, sourceUrl)
  }
}
