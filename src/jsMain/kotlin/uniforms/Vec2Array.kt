package vision.gears.webgpumath

import org.khronos.webgl.Float32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.reflect.KProperty
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.cos
import kotlin.math.sin

class Vec2Array(backingStorage: Float32Array?, startIndex: Int = 0, endIndex: Int = 0) : VecArray() {

  constructor(size : Int) : this(null, size, size) {}

  override var storage = backingStorage?.subarray(startIndex*2, endIndex*2)?:Float32Array(startIndex*2)

  override fun set(vararg values : Float) : Vec2Array {
    for(i in 0 until storage.length) {
      storage[i] = values.getOrNull(i%2) ?: 0.0f
    }
    return this
  }

  operator fun get(i : Int) : Vec2{
    return Vec2(storage, i*2)
  }

  fun subarray(begin : Int, end : Int) : Vec2Array {
    return Vec2Array(storage, begin, end)
  }

  fun setNormalized(b : Vec2Array) {
    for(i in 0 until storage.length step 2) {
      val l2 =
        b.storage[i  ] * b.storage[i  ] +
        b.storage[i+1] * b.storage[i+1]
      val linv = 1 / sqrt(l2)
      storage[i  ] = b.storage[i  ] * linv
      storage[i+1] = b.storage[i+1] * linv
    }
  }

  fun setAffineTransformed(v : Vec2Array, m : Mat4) {  
    for(i in 0 until storage.length step 2) {
      storage[i+0] =
         v.storage[i+0] * m.storage[ 0] +
         v.storage[i+1] * m.storage[ 4] +
                          m.storage[12]
      storage[i+1] =
         v.storage[i+0] * m.storage[ 1] +
         v.storage[i+1] * m.storage[ 5] +
                          m.storage[13]
    }
  }

  fun setAffineNormalTransformed(v : Vec2Array, m : Mat4) {  
    for(i in 0 until storage.length step 2) {
      storage[i+0] =
         v.storage[i+0] * m.storage[ 0] +
         v.storage[i+1] * m.storage[ 4]
      storage[i+1] =
         v.storage[i+0] * m.storage[ 1] +
         v.storage[i+1] * m.storage[ 5]
    }
  }  

  fun setCosSin(alphas : Vec1Array){
    for(i in 0 until storage.length step 2) {
      this.storage[i+0] = cos(alphas.storage[i/2])
      this.storage[i+1] = sin(alphas.storage[i/2])
    }
  }

  operator fun provideDelegate(
      provider: UniformProvider,
      property: KProperty<*>) : Vec2Array {
    return provider.bindUniform(property.name, this)
  }

  operator fun getValue(provider: UniformProvider, property: KProperty<*>): Vec2Array {
    return this
  }

  operator fun setValue(provider: UniformProvider, property: KProperty<*>, value: Vec2Array) {
    set(value)
  }  

} 