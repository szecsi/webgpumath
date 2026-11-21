package vision.gears.ecs

import kotlin.test.*

class Tag() : Component()
class Value(val v : Int) : Component()

class WorldTest {
  @Test
  fun handlesAreNeverReused() {
    val w = World()
    val a = w.create{}
    w.destroy(a)
    w.flush()
    val b = w.create{}
    assertNotEquals(a, b)
    assertFalse(w.isAlive(a))
    assertTrue(w.isAlive(b))
  }

  @Test
  fun destroyTakesEffectAtFlush() {
    val w = World()
    val a = w.create { ww -> ww += Tag() }
    w.destroy(a)
    assertTrue(w.isAlive(a))
    w.flush()
    assertFalse(w.isAlive(a))
    assertNull(w.get<Tag>(a))
    assertTrue(w.storage<Tag>().isEmpty())
  }

  @Test
  fun storagesStaySortedAndDense() {
    val w = World()
    val es = List(10) { w.create { ww -> ww += Value(10) } }
    w.destroy(es[3]); w.destroy(es[7]); w.flush()
    // an older entity gets a component later: inserted in the middle
    //TODO: endow and strip
    w.endow(es[1]) { ww -> ww += Value(99) }
    w.endow(es[5]) { ww -> ww += Tag() }
    w.endow(es[2]) { ww -> ww += Tag() }
    val handles = w.storage<Value>().map { it.entity }
    assertEquals(handles.sorted(), handles)
    assertEquals(8, handles.size)
    assertEquals(listOf(es[2], es[5]), w.storage<Tag>().map { it.entity })
    assertEquals(99, w.get<Value>(es[1])!!.v)
  }

  @Test
  fun refFindsMovedComponentAndRemembersRow() {
    val w = World()
    val es = List(5) { w.create { ww -> ww += Value(it) } }
    val ref = Ref(es[4])
    assertEquals(4, w.get<Value>(ref)!!.v)
    assertEquals(4, ref.hint)
    w.destroy(es[0]); w.destroy(es[1]); w.flush()
    assertEquals(4, w.get<Value>(ref)!!.v)
    assertEquals(2, ref.hint)
    // the remembered row is right: found without searching, and stays
    assertEquals(4, w.get<Value>(ref)!!.v)
    assertEquals(2, ref.hint)
    w.destroy(es[4]); w.flush()
    assertNull(w.get<Value>(ref))
  }

  @Test
  fun frozenEntitiesCannotBeDestroyed() {
    val w = World()
    val a = w.create{}
    w.freezeEntities()
    val b = w.create{}
    assertFailsWith<IllegalArgumentException> { w.destroy(a) }
    w.destroy(b)
    w.flush()
    assertTrue(w.isAlive(a))
    assertFalse(w.isAlive(b))
  }

  @Test
  fun registryRowsStayValidWhenAdding() {
    val first = Registry<String>()
    val second = Registry<Int>()   // refers to rows of first
    val r = first.addAll("a", "b", "c")
    assertEquals(0, r.first); assertEquals(3, r.count)
    assertEquals("b", first[r[1]])
    second.add(r[2])
    // adding upstream after downstream has rows: the old numbers still point to the same things
    assertEquals(3, first.add("d"))
    second.add(3)
    assertEquals("c", first[second[0]])
    assertEquals("d", first[second[1]])
  }
}
