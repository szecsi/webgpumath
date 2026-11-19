package vision.gears.webgpumath

/**
 * What a render pipeline needs to know of a geometry: the formats of its vertex
 * attributes, its topology, and for an indexed strip, the index format.
 * Geometries that agree in these can be drawn with the same pipelines.
 *
 * Equal layouts are one object: get them with `VertexLayout.of`. A Geometry does
 * this when it is created.
 */
class VertexLayout private constructor(
    /** Attribute name -> GPUVertexFormat, e.g. "vertexPosition" -> "float32x3". */
    val formats : Map<String, String>,
    /** A GPUPrimitiveTopology, e.g. "triangle-list". */
    val topology : String,
    /** The index format of an indexed strip, which marks where a strip restarts; null for lists and for drawing without indices. */
    val stripIndexFormat : String?,
    /** The same for equal layouts, different for different ones. */
    val key : String) {

  /** Unique among layouts. */
  val id = nextResourceId()

  companion object {
    private val pool = HashMap<String, VertexLayout>()

    fun keyOf(formats : Map<String, String>, topology : String, stripIndexFormat : String?) : String =
      formats.entries.sortedBy { it.key }.joinToString(",", postfix = "/$topology/$stripIndexFormat") { "${it.key}:${it.value}" }

    /** The layout with these properties: an existing one, or a new one. */
    fun of(formats : Map<String, String>, topology : String, stripIndexFormat : String?) : VertexLayout {
      val key = keyOf(formats, topology, stripIndexFormat)
      return pool.getOrPut(key) { VertexLayout(HashMap(formats), topology, stripIndexFormat, key) }
    }

    /** The number of bytes of one vertex's value of an attribute with GPUVertexFormat `format`. */
    fun strideOf(format : String) : Int {
      val componentCount = format.substringAfter('x', "1").toInt()
      val componentType = format.substringBefore('x')
      val componentSize = when {
        componentType.endsWith("32") -> 4
        componentType.endsWith("16") || componentType.endsWith("16norm") || componentType.endsWith("16float") -> 2
        else -> 1
      }
      return componentCount * componentSize
    }
  }
}
