package vision.gears.ecs

import kotlin.reflect.KClass

/**
 * Data that belongs to an entity. An entity is just an id: what it is and what
 * it does follows from the components it has.
 */
interface Component {
  val entity : Int

  /**
   * Called when the component is removed from the world, or replaced by another.
   * Override it to give back what the component holds, such as its place in a buffer on the GPU.
   */
  fun dispose() {}
}

/**
 * The entities and their components. Systems are functions that run queries on it:
 *
 *     world.query<Position>().with<Velocity>().without<Frozen>().each { p, v -> ... }
 */
class World {
  @PublishedApi internal val storages = HashMap<KClass<*>, MutableList<out Component>>()

  /** The components of one class, sorted by entity id. */
  @Suppress("UNCHECKED_CAST")
  inline fun <reified T : Component> storage() : MutableList<T> =
    storages.getOrPut(T::class) { mutableListOf<T>() } as MutableList<T>

  /**
   * Adds the component to its entity, replacing the one of the same class if there is one.
   * Not to be called from inside a query of the same component class: use `defer` there.
   */
  inline fun <reified T : Component> set(c : T) {
    val s = storage<T>()
    val i = s.binarySearchBy(c.entity) { it.entity }
    if(i >= 0) {
      val old = s[i]
      s[i] = c
      if(old !== c) old.dispose()
    } else {
      s.add(-i - 1, c)
    }
  }

  inline fun <reified T : Component> get(entity : Int) : T? {
    val s = storage<T>()
    val i = s.binarySearchBy(entity) { it.entity }
    return if(i >= 0) s[i] else null
  }

  /** Not to be called from inside a query of the same component class: use `defer` there. */
  inline fun <reified T : Component> remove(entity : Int) {
    val s = storage<T>()
    val i = s.binarySearchBy(entity) { it.entity }
    if(i >= 0) s.removeAt(i).dispose()
  }

  inline fun <reified A : Component> query() = Query1(this, storage<A>(), emptyList())

  // Entities. The ids of destroyed entities are used again.
  private val alive = HashSet<Int>()
  private val freeEntities = ArrayList<Int>()
  private var nextEntity = 0

  fun create() : Int {
    val entity = if(freeEntities.isNotEmpty()) freeEntities.removeAt(freeEntities.size - 1) else nextEntity++
    alive += entity
    return entity
  }

  /** False for an entity that was destroyed. (Its id may be given to a new entity later.) */
  fun isAlive(entity : Int) : Boolean = entity in alive

  /** Removes all components of the entity, at the next `flush`. */
  fun destroy(entity : Int) = defer {
    if(alive.remove(entity)) {
      for(s in storages.values) {
        val i = s.binarySearchBy(entity) { it.entity }
        if(i >= 0) s.removeAt(i).dispose()
      }
      freeEntities += entity
    }
  }

  // Storages must not change while a query is running over them. Systems queue
  // their structural changes (adding and removing components) here.
  private val deferred = ArrayList<() -> Unit>()

  fun defer(command : () -> Unit) {
    deferred += command
  }

  /** Runs the deferred commands. Call it between systems, not from inside a query. */
  fun flush() {
    var i = 0
    while(i < deferred.size) deferred[i++]()
    deferred.clear()
  }
}
