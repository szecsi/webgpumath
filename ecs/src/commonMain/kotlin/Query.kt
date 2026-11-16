package vision.gears.ecs

// The number of component classes a query asks for is in its type: query<A>() is
// a Query1<A>, .with<B>() makes it a Query2<A, B>, and so on. Each has one `each`,
// with as many parameters. (Kotlin has no generics with a variable number of types.)

/** Shared by every query: the excluded storages, and the n-way merge join. */
abstract class QueryBase(
  @PublishedApi internal val world : World,
  @PublishedApi internal val excluded : List<List<Component>>
) {
  @PublishedApi internal fun isExcluded(entity : Int) =
    excluded.any { s -> s.binarySearchBy(entity) { it.entity } >= 0 }

  // All lists are sorted by entity: advance in each to the largest entity seen,
  // until all of them are at the same one.
  @PublishedApi internal inline fun join(lists : Array<out List<Component>>, emit : (IntArray) -> Unit) {
    val n = lists.size
    val idx = IntArray(n)
    while(true) {
      var target = Int.MIN_VALUE
      for(k in 0 until n) {
        if(idx[k] >= lists[k].size) return
        target = maxOf(target, lists[k][idx[k]].entity)
      }
      var matched = true
      for(k in 0 until n) {
        val l = lists[k]
        while(idx[k] < l.size && l[idx[k]].entity < target) idx[k]++
        if(idx[k] >= l.size) return
        if(l[idx[k]].entity != target) matched = false
      }
      if(matched) {
        if(!isExcluded(target)) emit(idx)
        for(k in 0 until n) idx[k]++
      }
    }
  }
}

class Query1<A : Component>(
  world : World, @PublishedApi internal val a : List<A>, excluded : List<List<Component>>
) : QueryBase(world, excluded) {
  inline fun <reified B : Component> with() = Query2(world, a, world.storage<B>(), excluded)
  inline fun <reified X : Component> without() = Query1(world, a, excluded + listOf(world.storage<X>()))
  inline fun each(f : (A) -> Unit) { for(c in a) if(!isExcluded(c.entity)) f(c) }
}

class Query2<A : Component, B : Component>(
  world : World,
  @PublishedApi internal val a : List<A>, @PublishedApi internal val b : List<B>,
  excluded : List<List<Component>>
) : QueryBase(world, excluded) {
  inline fun <reified C : Component> with() = Query3(world, a, b, world.storage<C>(), excluded)
  inline fun <reified X : Component> without() = Query2(world, a, b, excluded + listOf(world.storage<X>()))
  inline fun each(f : (A, B) -> Unit) = join(arrayOf(a, b)) { i -> f(a[i[0]], b[i[1]]) }
}

class Query3<A : Component, B : Component, C : Component>(
  world : World,
  @PublishedApi internal val a : List<A>, @PublishedApi internal val b : List<B>,
  @PublishedApi internal val c : List<C>,
  excluded : List<List<Component>>
) : QueryBase(world, excluded) {
  inline fun <reified D : Component> with() = Query4(world, a, b, c, world.storage<D>(), excluded)
  inline fun <reified X : Component> without() = Query3(world, a, b, c, excluded + listOf(world.storage<X>()))
  inline fun each(f : (A, B, C) -> Unit) = join(arrayOf(a, b, c)) { i -> f(a[i[0]], b[i[1]], c[i[2]]) }
}

class Query4<A : Component, B : Component, C : Component, D : Component>(
  world : World,
  @PublishedApi internal val a : List<A>, @PublishedApi internal val b : List<B>,
  @PublishedApi internal val c : List<C>, @PublishedApi internal val d : List<D>,
  excluded : List<List<Component>>
) : QueryBase(world, excluded) {
  inline fun <reified X : Component> without() = Query4(world, a, b, c, d, excluded + listOf(world.storage<X>()))
  inline fun each(f : (A, B, C, D) -> Unit) = join(arrayOf(a, b, c, d)) { i -> f(a[i[0]], b[i[1]], c[i[2]], d[i[3]]) }
}
