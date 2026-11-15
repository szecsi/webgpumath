package vision.gears.webgpumath

/** A member of a WGSL struct. For arrays, `typeName` is the element type. */
class MemberLayout(
  val name : String,
  val typeName : String,
  val offset : Int,
  val size : Int,
  val arrayCount : Int,   // 0 if the member is not an array
  val arrayStride : Int ) {
  val isArray get() = arrayCount > 0
  val signature get() = "$name:$typeName@$offset" + (if(isArray) "[$arrayCount/$arrayStride]" else "")
}

class StructLayout(val name : String, val size : Int, val members : List<MemberLayout>) {
  private val membersByName = members.associateBy { it.name }
  fun member(name : String) : MemberLayout? = membersByName[name]
  /** Two declarations are interchangeable if their signatures are equal. */
  val signature = members.joinToString(prefix = "{", postfix = "}/$size") { it.signature }
}

enum class BindingKind { UNIFORM, STORAGE, TEXTURE, SAMPLER }

/** A module-scope resource variable: `@group(g) @binding(b) var... name : type`. */
class BindingInfo(
  val name : String,
  val group : Int,
  val binding : Int,
  val kind : BindingKind,
  val typeName : String,
  val struct : StructLayout?, // the struct, or the element struct of an array
  val isArray : Boolean,
  val arrayCount : Int,       // 0 for runtime-sized arrays
  val arrayStride : Int,
  val size : Int ) {
  val signature get() = "$group/$binding/$kind/$typeName/${struct?.signature}"
}

class VertexInput(val name : String, val location : Int, val typeName : String)

/**
 * Everything webgpumath needs to know about one WGSL source text:
 * the resource variables it declares, which of them its entry points use,
 * and the vertex inputs.
 */
class ShaderReflection(val source : String, val debugName : String = "") {
  /** All declared resource variables by name. */
  val bindings = HashMap<String, BindingInfo>()
  /** Names of the resource variables used by the entry points, directly or through function calls. */
  val used = HashSet<String>()
  val vertexInputs = ArrayList<VertexInput>()
  var vertexEntryPoint : String? = null
  var fragmentEntryPoint : String? = null

  init {
    val reflect : dynamic = try {
      WgslReflect(source)
    } catch(e : dynamic) {
      throw Error("Could not parse WGSL source $debugName: ${e.message}")
    }
    addVariables(reflect.uniforms, BindingKind.UNIFORM)
    addVariables(reflect.storage, BindingKind.STORAGE)
    addVariables(reflect.textures, BindingKind.TEXTURE)
    addVariables(reflect.samplers, BindingKind.SAMPLER)

    val vertexFunctions = reflect.entry.vertex.unsafeCast<Array<dynamic>>()
    val fragmentFunctions = reflect.entry.fragment.unsafeCast<Array<dynamic>>()
    // (no scope functions on dynamic values: `?.let` would be compiled to a JavaScript method call)
    if(vertexFunctions.isNotEmpty()) {
      val fn = vertexFunctions[0]
      vertexEntryPoint = fn.name.unsafeCast<String>()
      for(input in fn.inputs.unsafeCast<Array<dynamic>>()) {
        if(input.locationType == "location") {
          vertexInputs += VertexInput(input.name.unsafeCast<String>(), input.location.unsafeCast<Int>(), typeNameOf(input.type))
        }
      }
    }
    if(fragmentFunctions.isNotEmpty()) {
      fragmentEntryPoint = fragmentFunctions[0].name.unsafeCast<String>()
    }
    for(fn in vertexFunctions + fragmentFunctions) {
      for(resource in fn.resources.unsafeCast<Array<dynamic>>()) {
        used += resource.name.unsafeCast<String>()
      }
    }
  }

  private fun typeNameOf(type : dynamic) : String =
    if(type == null) "" else type.getTypeName().unsafeCast<String>()

  private fun structLayoutOf(type : dynamic) : StructLayout {
    val members = type.members.unsafeCast<Array<dynamic>>().map { m ->
      val isArray = m.type.isArray.unsafeCast<Boolean>()
      MemberLayout(
        name = m.name.unsafeCast<String>(),
        typeName = if(isArray) typeNameOf(m.type.format) else typeNameOf(m.type),
        offset = m.offset.unsafeCast<Int>(),
        size = m.size.unsafeCast<Int>(),
        arrayCount = if(isArray) m.type.count.unsafeCast<Int>() else 0,
        arrayStride = if(isArray) m.type.stride.unsafeCast<Int>() else 0 )
    }
    return StructLayout(type.name.unsafeCast<String>(), type.size.unsafeCast<Int>(), members)
  }

  private fun addVariables(variables : dynamic, kind : BindingKind) {
    for(v in variables.unsafeCast<Array<dynamic>>()) {
      val name = v.name.unsafeCast<String>()
      val type = v.type
      val isArray = type.isArray.unsafeCast<Boolean>()
      val elementType = if(isArray) type.format else type
      val isBuffer = kind == BindingKind.UNIFORM || kind == BindingKind.STORAGE
      if(isBuffer && !elementType.isStruct.unsafeCast<Boolean>()) {
        throw Error("WGSL variable '$name' in $debugName has type ${typeNameOf(type)}. " +
          "webgpumath needs uniform and storage variables to be structs or arrays of structs.")
      }
      bindings[name] = BindingInfo(
        name = name,
        group = v.group.unsafeCast<Int>(),
        binding = v.binding.unsafeCast<Int>(),
        kind = kind,
        typeName = typeNameOf(type),
        struct = if(isBuffer) structLayoutOf(elementType) else null,
        isArray = isArray,
        arrayCount = if(isArray) type.count.unsafeCast<Int>() else 0,
        arrayStride = if(isArray) type.stride.unsafeCast<Int>() else 0,
        size = type.size.unsafeCast<Int>() )
    }
  }
}
