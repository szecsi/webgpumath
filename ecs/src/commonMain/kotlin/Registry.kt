package vision.gears.ecs

/** Consecutive rows of a registry: `first` until `first + count - 1`. */
class Range(val first : Int, val count : Int) {
  val indices : IntRange get() = first until first + count
  operator fun contains(row : Int) = row >= first && row < first + count
  operator fun get(i : Int) : Int = first + i
}

/**
 * A list of things, referred to by their row: a number that never changes.
 *
 * Rows can only be added, at the end: never removed, replaced or reordered. So a row
 * number, once handed out, stays valid, and things in other registries (or components)
 * can refer to a row by its number. Adding to any registry at any time is safe.
 */
open class Registry<T> {
  private val list = ArrayList<T>()

  /** The rows, to read. */
  val rows : List<T> get() = list

  val size : Int get() = list.size

  /** Adds `item` as the next row, and returns its number. */
  open fun add(item : T) : Int {
    list += item
    return list.size - 1
  }

  /** Adds the items as consecutive rows. */
  fun addAll(vararg items : T) : Range {
    val first = rows.size
    for(item in items) add(item)
    return Range(first, items.size)
  }

  operator fun get(row : Int) : T = list[row]
}
