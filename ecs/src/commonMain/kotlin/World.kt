package vision.gears.ecs

import kotlin.reflect.KClass

/**
 * Data that belongs to an entity. An entity is just a handle: what it is and what
 * it does follows from the components it has.
 */
open class Component {
  var entity : Int = -1
    internal set

  /**
   * Called when the component is removed from the world, or replaced by another.
   * Override it to give back what the component holds.
   */
  open fun dispose() {}
}

/**
 * A reference to an entity that remembers where it was found last.
 *
 * Every storage is sorted by entity handle, and a component's row in it changes
 * when entities before it are destroyed, or when an older entity gets a component.
 * `world.get<T>(ref)` tries the remembered row first, and only searches when the
 * component is not there any more; then it remembers the new row. A Ref has one
 * remembered row, so it pays off for the component class it is mostly used with.
 */
class Ref(val entity : Int) {
  @PublishedApi internal var hint = 0
}

/**
 * The entities and their components. Systems are functions that run queries on it:
 *
 *     world.query<Position>().with<Velocity>().without<Frozen>().each { p, v -> ... }
 *
 * Entity handles count up from 0 and are never given out again. So every storage,
 * sorted by handle, is in the order the entities were created: a parent created
 * before its children comes before them.
 */
class World {
  @PublishedApi internal val storages = HashMap<KClass<*>, MutableList<out Component>>()

  /** The components of one class, sorted by entity handle, without gaps. */
  @Suppress("UNCHECKED_CAST")
  inline fun <reified T : Component> storage() : MutableList<T> =
    storages.getOrPut(T::class) { mutableListOf<T>() } as MutableList<T>


  inline fun <reified T : Component> get(entity : Int) : T? {
    val s = storage<T>()
    val i = s.binarySearchBy(entity) { it.entity }
    return if(i >= 0) s[i] else null
  }

  /** The component of the referenced entity: at the row where it was found last, or searched for. */
  inline fun <reified T : Component> get(ref : Ref) : T? {
    val s = storage<T>()
    val hint = ref.hint
    if(hint < s.size && s[hint].entity == ref.entity) return s[hint]
    val i = s.binarySearchBy(ref.entity) { it.entity }
    if(i < 0) return null
    ref.hint = i
    return s[i]
  }

  /** Not to be called from inside a query of the same component class: use `defer` there. */
  inline fun <reified T : Component> remove(entity : Int) {
    val s = storage<T>()
    val i = s.binarySearchBy(entity) { it.entity }
    if(i >= 0) s.removeAt(i).dispose()
  }

  inline fun <reified A : Component> query() = Query1(this, storage<A>(), emptyList())

  // The handles of the living entities, in increasing order.
  private val entities = ArrayList<Int>()
  private var nextEntity = 0

  /**
   * Entities with a handle below this cannot be destroyed. Since nothing before them
   * is ever removed, their components never move in the storages.
   */
  var frozenRange = 0
    private set

  /** Makes the entities created so far permanent: see `frozenRange`. */
  fun freezeEntities() {
    frozenRange = nextEntity
  }

  inner class Populator internal constructor(val entity : Int) {
    inline operator fun <reified T : Component> plusAssign(c : T){
      val s = storage<T>()
      set(s, c)
    }
    fun <T : Component> set(s : MutableList<T>, c : T){
      c.entity = entity
      val i = s.binarySearchBy(c.entity) { it.entity }
      if(i >= 0) {
        val old = s[i]
        s[i] = c
        if(old !== c) old.dispose()
      } else {
        s.add(-i - 1, c)
      }
    }
  }

  /** 
   * Creates an entity, and lets `populator` add its components. Returns its handle. 
   * Not to be called from inside a query of the same component class: use `defer` there.
   */
  fun create(populator : (Populator) -> Unit) : Int {
    val entity = nextEntity++
    // the newest handle is the largest: the list stays sorted
    entities += entity
    return endow(entity, populator)
  }

  /** 
   * Lets `populator` add components to an exisiting entity. Returns its handle. 
   * Not to be called from inside a query of the same component class: use `defer` there.
   */
  fun endow(entity : Int, populator : (Populator) -> Unit) : Int {
    populator.invoke(Populator(entity))
    return entity
  }

  /** False for an entity that was destroyed, or never created. Handles are not used again. */
  fun isAlive(entity : Int) : Boolean = entities.binarySearch(entity) >= 0

  /** Removes the entity and all its components, at the next `flush`. */
  fun destroy(entity : Int) {
    if(entity < frozenRange) {
      throw IllegalArgumentException("Entity $entity is below the frozen range ($frozenRange): it cannot be destroyed.")
    }
    defer {
      val e = entities.binarySearch(entity)
      if(e >= 0) {
        entities.removeAt(e)
        for(s in storages.values) {
          val i = s.binarySearchBy(entity) { it.entity }
          if(i >= 0) s.removeAt(i).dispose()
        }
      }
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
