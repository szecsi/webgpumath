package vision.gears.webgpumath

import org.khronos.webgl.Float32Array
import org.khronos.webgl.Int32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.reflect.KProperty

/** A texture that can be bound to a shader: an image view and the sampler that filters it. */
interface Texture {
  val view : GPUTextureView
  val sampler : GPUSampler
  /** Must change whenever `view` or `sampler` is replaced, e.g. when an image finishes loading. */
  val resourceId : Int
  /** The GPUTextureViewDimension of `view`: "2d", "cube", "2d-array", "3d", ... */
  val viewDimension : String get() = "2d"
}

interface Uniform {
  fun set(other: Uniform) : Uniform
  fun set(vararg values : Float) : Uniform
  fun set(firstValue : Int, vararg values : Int) : Uniform
  fun set(firstTexture : Texture, vararg moreTextures : Texture)
  fun getStorageSize() : Int
  infix fun gets(other : Uniform){
    set(other)
  }
}

interface UniformFloat : Uniform {
  /** The memory it views. Replaced when that memory moves (UniformBlock.moveTo). */
  var storage : Float32Array

  override fun getStorageSize() : Int{
    return storage.length
  }

  override fun set(other: Uniform) : Uniform {
    if(other is UniformFloat){
      for(i : Int in 0 until storage.length) {
        storage[i] = if(i < other.storage.length) other.storage[i] else 0.0f
      }
    } else {
      throw Error("Cannot set a uniform of floats from a non-float uniform.")
    }
    return this
  }

  override fun set(firstValue : Int, vararg values : Int) : Uniform {
    throw Error("Cannot set int values to a float uniform.")
  }

  override fun set(firstTexture : Texture, vararg moreTextures : Texture){
    throw Error("Cannot set a texture to a non-texture uniform.")
  }
}

interface UniformInt : Uniform {
  /** The memory it views. Replaced when that memory moves (UniformBlock.moveTo). */
  var storage : Int32Array

  override fun getStorageSize() : Int{
    return storage.length
  }

  override fun set(other: Uniform) : Uniform {
    if(other is UniformInt){
      for(i : Int in 0 until storage.length) {
        storage[i] = if(i < other.storage.length) other.storage[i] else 0
      }
    } else {
      throw Error("Cannot set a uniform of ints from a non-int uniform.")
    }
    return this
  }

  override fun set(vararg values : Float) : Uniform {
    if(values.size == 0){
      for(i : Int in 0 until storage.length) {
        storage[i] = 0
      }
    } else {
      throw Error("Cannot set float values to an int uniform.")
    }
    return this
  }

  override fun set(firstTexture : Texture, vararg moreTextures : Texture){
    throw Error("Cannot set a texture to a non-texture uniform.")
  }
}

/**
 * A texture binding of a provider. In WGSL, the variable `material_colorTexture`
 * is the texture `colorTexture` of the provider `material`.
 */
abstract class UniformSampler(var debugName : String) : Uniform {
  var texture : Texture? = null

  override fun getStorageSize() : Int = 1

  override fun set(other: Uniform) : Uniform {
    if(other is UniformSampler){
      texture = other.texture
    } else {
      throw Error("Cannot set a uniform of textures from a non-texture uniform.")
    }
    return this
  }

  override fun set(firstTexture : Texture, vararg moreTextures : Texture){
    if(moreTextures.isNotEmpty()) {
      throw Error("Texture arrays are not supported: '$debugName' takes a single texture.")
    }
    texture = firstTexture
  }

  override fun set(vararg values : Float) : Uniform {
    throw Error("Cannot set float values to a texture uniform.")
  }

  override fun set(firstValue : Int, vararg values : Int) : Uniform {
    throw Error("Cannot set int values to a texture uniform.")
  }
}

class Sampler2D(debugName : String = "") : UniformSampler(debugName) {
  operator fun provideDelegate(owner: UniformOwner, property: KProperty<*>) : Sampler2D {
    debugName = property.name
    return owner.provider.bindUniform(property.name, this)
  }
  operator fun getValue(owner: UniformOwner, property: KProperty<*>): Sampler2D = this
  operator fun setValue(owner: UniformOwner, property: KProperty<*>, value: Sampler2D) { set(value) }
}

class SamplerCube(debugName : String = "") : UniformSampler(debugName) {
  operator fun provideDelegate(owner: UniformOwner, property: KProperty<*>) : SamplerCube {
    debugName = property.name
    return owner.provider.bindUniform(property.name, this)
  }
  operator fun getValue(owner: UniformOwner, property: KProperty<*>): SamplerCube = this
  operator fun setValue(owner: UniformOwner, property: KProperty<*>, value: SamplerCube) { set(value) }
}

class Sampler3D(debugName : String = "") : UniformSampler(debugName) {
  operator fun provideDelegate(owner: UniformOwner, property: KProperty<*>) : Sampler3D {
    debugName = property.name
    return owner.provider.bindUniform(property.name, this)
  }
  operator fun getValue(owner: UniformOwner, property: KProperty<*>): Sampler3D = this
  operator fun setValue(owner: UniformOwner, property: KProperty<*>, value: Sampler3D) { set(value) }
}

/**
 * Creates the uniform object for a struct member as a view into the memory
 * that holds the struct. `baseOffset` is the byte offset of the struct.
 */
fun makeUniformView(member : MemberLayout, floats : Float32Array, ints : Int32Array, baseOffset : Int) : Uniform {
  val o = (baseOffset + member.offset) / 4
  val n = member.arrayCount
  if(!member.isArray) {
    when(member.typeName) {
      "f32" ->     return Vec1(floats, o)
      "vec2f" ->   return Vec2(floats, o)
      "vec3f" ->   return Vec3(floats, o)
      "vec4f" ->   return Vec4(floats, o)
      "mat4x4f" -> return Mat4(floats, o)
      "i32", "u32" ->     return IVec1(ints, o)
      "vec2i", "vec2u" -> return IVec2(ints, o)
      "vec3i", "vec3u" -> return IVec3(ints, o)
      "vec4i", "vec4u" -> return IVec4(ints, o)
    }
  } else {
    when(member.typeName) {
      "f32" ->     if(member.arrayStride == 4)  return Vec1Array(floats.subarray(o, o + n), 0, n)
      "vec2f" ->   if(member.arrayStride == 8)  return Vec2Array(floats.subarray(o, o + n * 2), 0, n)
      "vec4f" ->   return Vec4Array(floats.subarray(o, o + n * 4), 0, n)
      "mat4x4f" -> return Mat4Array(floats.subarray(o, o + n * 16), 0, n)
      "i32", "u32" -> if(member.arrayStride == 4) return IVec1Array(ints.subarray(o, o + n), 0, n)
      "vec3f" -> throw Error("Member '${member.name}' is an array of vec3f, whose elements are padded to 16 bytes in WGSL. Use an array of vec4f.")
    }
    throw Error("Member '${member.name}' is an array of ${member.typeName} with element stride ${member.arrayStride}, which webgpumath cannot represent. In uniform buffers, use arrays of vec4f or mat4x4f.")
  }
  throw Error("Member '${member.name}' has type ${member.typeName}, which webgpumath cannot represent.")
}

/** The uniform class that reflects a WGSL texture type. */
fun makeTextureUniform(typeName : String, debugName : String) : UniformSampler =
  when(textureViewDimension(typeName)) {
    "cube" -> SamplerCube(debugName)
    "3d" -> Sampler3D(debugName)
    else -> Sampler2D(debugName)
  }

/** The GPUTextureViewDimension of a WGSL texture type such as `texture_cube<f32>`. */
fun textureViewDimension(typeName : String) : String {
  val base = typeName.substringBefore("<").removePrefix("texture_").removePrefix("depth_").removePrefix("multisampled_")
  return when(base) {
    "1d" -> "1d"
    "2d_array" -> "2d-array"
    "cube" -> "cube"
    "cube_array" -> "cube-array"
    "3d" -> "3d"
    else -> "2d"
  }
}
