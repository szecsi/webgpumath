package vision.gears.webgpumath

/**
 * Textures by name: "colorTexture" is what WGSL variable `material_colorTexture`
 * receives, when a provider claiming "material" answers `textureFor` from it.
 *
 * Textures, unlike uniform values, cannot differ between the instances of a draw call:
 * they are bound once per call. So things drawn together need the same textures, and
 * the id of their TextureBindings tells whether they have them.
 *
 * A TextureBindings does not change once made, and equal ones are one object: get
 * them with `TextureBindings.of`.
 *
 *     material.textures = TextureBindings.of("colorTexture" to slowpokeTexture)
 */
class TextureBindings private constructor(
    textures : Map<String, Texture>,
    /** The same for equal sets of textures, different for different ones. */
    val key : String) {

  val textures : Map<String, Texture> = HashMap(textures)

  /** Unique among texture bindings. */
  val id = nextResourceId()

  operator fun get(name : String) : Texture? = textures[name]

  companion object {
    private val pool = HashMap<String, TextureBindings>()
    // A number for each texture, for the key. Not its resourceId: that changes when the
    // texture's image has loaded, but it is still the same texture.
    private val textureIds = HashMap<Texture, Int>()

    fun keyOf(textures : Map<String, Texture>) : String =
      textures.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${textureIds.getOrPut(it.value) { nextResourceId() }}" }

    /** The bindings of these textures: an existing one, or a new one. */
    fun of(vararg textures : Pair<String, Texture>) : TextureBindings = of(mapOf(*textures))

    /** The bindings of these textures: an existing one, or a new one. */
    fun of(textures : Map<String, Texture>) : TextureBindings {
      val key = keyOf(textures)
      return pool.getOrPut(key) { TextureBindings(textures, key) }
    }

    /** No textures. (Last: making it needs the pool above.) */
    val none = of(emptyMap())
  }
}
