package vision.gears.webgpumath

import org.khronos.webgl.Int32Array
import org.khronos.webgl.get
import org.khronos.webgl.set
import kotlin.reflect.KProperty
import kotlin.random.Random
import kotlin.math.pow
import kotlin.math.sqrt

class IVec1Array(backingStorage: Int32Array?, startIndex: Int = 0, endIndex: Int = 0) : IVecArray() {

  constructor(size : Int) : this(null, size, size) {}

  override var storage = backingStorage?.subarray(startIndex, endIndex)?:Int32Array(startIndex)

  override fun set(firstValue : Int, vararg values : Int) : IVec1Array {
    storage[0] = firstValue
    for(i in 1 until storage.length) {
      storage[i] = values.getOrNull(i-1) ?: 0
    }
    return this
  }

  operator fun get(i : Int) : IVec1{
    return IVec1(storage, i)
  }

  fun subarray(begin : Int, end : Int) : IVec1Array {
    return IVec1Array(storage, begin, end)
  }

  operator fun provideDelegate(
      provider: UniformProvider,
      property: KProperty<*>) : IVec1Array {
    return provider.bindUniform(property.name, this)
  }

  operator fun getValue(provider: UniformProvider, property: KProperty<*>): IVec1Array {
    return this
  }

  operator fun setValue(provider: UniformProvider, property: KProperty<*>, value: IVec1Array) {
    set(value)
  }  

} 