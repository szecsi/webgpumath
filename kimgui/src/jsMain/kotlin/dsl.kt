package vision.gears.kimgui

import kotlin.reflect.KMutableProperty0
import vision.gears.webgpumath.Vec2
import vision.gears.webgpumath.Vec3
import vision.gears.webgpumath.Vec4

// Dear ImGui in Kotlin style.
//
// - What has a Begin and an End is a function with a block: window("name") { ... }.
// - What is pushed and popped is a function with a block: withItemWidth(100f) { ... }.
// - A widget that edits a value takes a property as ::name, or a webgpumath vector.
//   Its block runs when the user changed the value.
// - A widget that is clicked runs its block when clicked.
//
// ImGui itself wants its own ImVec2 and ImVec4 for sizes and colors.
object dsl {

  // Windows

  inline fun window(name : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.Begin(name, null, flags)) {
      block()
    }
    ImGui.End()
  }

  // a window with a close button: `open` is set to false when it is clicked
  inline fun window(name : String, open : KMutableProperty0<Boolean>, flags : Int = 0, block : () -> Unit) {
    if(!open.get()) return
    val v = arrayOf(true)
    if(ImGui.Begin(name, v, flags)) {
      block()
    }
    ImGui.End()
    if(!v[0]) open.set(false)
  }

  inline fun child(strId : String, size : ImVec2 = ImVec2(0.0f, 0.0f), childFlags : Int = 0, windowFlags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginChild(strId, size, childFlags, windowFlags)) {
      block()
    }
    ImGui.EndChild()
  }

  // Stacks

  // a null font is the current one, in another size
  inline fun withFont(font : ImFont?, size : Float, block : () -> Unit) {
    ImGui.PushFontFloat(font, size)
    block()
    ImGui.PopFont()
  }

  // idx is an ImGuiCol
  inline fun withStyleColor(idx : Int, color : ImVec4, block : () -> Unit) {
    ImGui.PushStyleColorImVec4(idx, color)
    block()
    ImGui.PopStyleColor()
  }

  inline fun withStyleColor(idx0 : Int, color0 : ImVec4, idx1 : Int, color1 : ImVec4, block : () -> Unit) {
    ImGui.PushStyleColorImVec4(idx0, color0)
    ImGui.PushStyleColorImVec4(idx1, color1)
    block()
    ImGui.PopStyleColor(2)
  }

  inline fun withStyleColor(idx0 : Int, color0 : ImVec4, idx1 : Int, color1 : ImVec4, idx2 : Int, color2 : ImVec4, block : () -> Unit) {
    ImGui.PushStyleColorImVec4(idx0, color0)
    ImGui.PushStyleColorImVec4(idx1, color1)
    ImGui.PushStyleColorImVec4(idx2, color2)
    block()
    ImGui.PopStyleColor(3)
  }

  // idx is an ImGuiStyleVar
  inline fun withStyleVar(idx : Int, value : Float, block : () -> Unit) {
    ImGui.PushStyleVar(idx, value)
    block()
    ImGui.PopStyleVar()
  }

  inline fun withStyleVar(idx : Int, value : ImVec2, block : () -> Unit) {
    ImGui.PushStyleVarImVec2(idx, value)
    block()
    ImGui.PopStyleVar()
  }

  inline fun withItemWidth(itemWidth : Float, block : () -> Unit) {
    ImGui.PushItemWidth(itemWidth)
    block()
    ImGui.PopItemWidth()
  }

  inline fun withTextWrapPos(wrapPosX : Float = 0.0f, block : () -> Unit) {
    ImGui.PushTextWrapPos(wrapPosX)
    block()
    ImGui.PopTextWrapPos()
  }

  // buttons in the block keep firing while they are held down
  inline fun withButtonRepeat(repeat : Boolean = true, block : () -> Unit) {
    ImGui.PushItemFlag(ImGuiItemFlags.ButtonRepeat, repeat)
    block()
    ImGui.PopItemFlag()
  }

  inline fun withClipRect(clipRectMin : ImVec2, clipRectMax : ImVec2, intersectWithCurrentClipRect : Boolean, block : () -> Unit) {
    ImGui.PushClipRect(clipRectMin, clipRectMax, intersectWithCurrentClipRect)
    block()
    ImGui.PopClipRect()
  }

  // widgets with the same label need different ids
  inline fun withID(id : String, block : () -> Unit) {
    ImGui.PushID(id)
    block()
    ImGui.PopID()
  }

  inline fun withID(id : Int, block : () -> Unit) {
    ImGui.PushIDInt(id)
    block()
    ImGui.PopID()
  }

  // the widgets in the block are greyed out and do not react
  inline fun disabled(disabled : Boolean = true, block : () -> Unit) {
    ImGui.BeginDisabled(disabled)
    block()
    ImGui.EndDisabled()
  }

  // Layout

  inline fun indent(indentW : Float = 0.0f, block : () -> Unit) {
    ImGui.Indent(indentW)
    block()
    ImGui.Unindent(indentW)
  }

  inline fun group(block : () -> Unit) {
    ImGui.BeginGroup()
    block()
    ImGui.EndGroup()
  }

  fun separator() {
    ImGui.Separator()
  }

  fun sameLine() {
    ImGui.SameLine()
  }

  fun sameLine(xPos : Float) {
    ImGui.SameLine(xPos)
  }

  fun newLine() {
    ImGui.NewLine()
  }

  // Text

  fun text(text : String) {
    ImGui.Text(text)
  }

  fun textWrapped(text : String) {
    ImGui.TextWrapped(text)
  }

  fun textColored(color : ImVec4, text : String) {
    ImGui.TextColored(color, text)
  }

  // About the widget made just before

  inline fun clicked(block : () -> Unit) {
    if(ImGui.IsItemClicked()) {
      block()
    }
  }

  inline fun hovered(flags : Int = 0, block : () -> Unit) {
    if(ImGui.IsItemHovered(flags)) {
      block()
    }
  }

  fun tooltipText(text : String) {
    ImGui.SetItemTooltip(text)
  }

  inline fun tooltip(block : () -> Unit) {
    if(ImGui.BeginTooltip()) {
      block()
      ImGui.EndTooltip()
    }
  }

  // Buttons

  inline fun button(label : String, block : () -> Unit) {
    if(ImGui.Button(label)) {
      block()
    }
  }

  inline fun button(label : String, size : ImVec2, block : () -> Unit) {
    if(ImGui.Button(label, size)) {
      block()
    }
  }

  inline fun smallButton(label : String, block : () -> Unit) {
    if(ImGui.SmallButton(label)) {
      block()
    }
  }

  inline fun invisibleButton(strId : String, size : ImVec2, block : () -> Unit) {
    if(ImGui.InvisibleButton(strId, size)) {
      block()
    }
  }

  // dir is an ImGuiDir
  inline fun arrowButton(strId : String, dir : Int, block : () -> Unit) {
    if(ImGui.ArrowButton(strId, dir)) {
      block()
    }
  }

  inline fun colorButton(label : String, color : ImVec4, block : () -> Unit) {
    if(ImGui.ColorButton(label, color)) {
      block()
    }
  }

  fun progressBar(fraction : Float, overlay : String = "", size : ImVec2 = ImVec2(-1.0f, 0.0f)) {
    ImGui.ProgressBar(fraction, size, overlay)
  }

  // Checkboxes and radio buttons

  inline fun checkbox(label : String, property : KMutableProperty0<Boolean>, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.Checkbox(label, v)) {
      property.set(v[0])
      block()
    }
  }

  // for a value that is not a property: the block gets the new value
  inline fun checkbox(label : String, value : Boolean, block : (Boolean) -> Unit) {
    val v = arrayOf(value)
    if(ImGui.Checkbox(label, v)) {
      block(v[0])
    }
  }

  // ticks and clears the bits of flagsValue in the property
  inline fun checkboxFlags(label : String, property : KMutableProperty0<Int>, flagsValue : Int, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.CheckboxFlagsIntPtr(label, v, flagsValue)) {
      property.set(v[0])
      block()
    }
  }

  inline fun radioButton(label : String, active : Boolean, block : () -> Unit) {
    if(ImGui.RadioButton(label, active)) {
      block()
    }
  }

  // one of several buttons for the same property: this one sets it to `value`
  inline fun radioButton(label : String, property : KMutableProperty0<Int>, value : Int, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.RadioButtonIntPtr(label, v, value)) {
      property.set(v[0])
      block()
    }
  }

  // Combo and list boxes

  // the property is the index of the chosen item
  inline fun combo(label : String, property : KMutableProperty0<Int>, items : List<String>, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.Combo(label, v, items.joinToString("") { it + "\u0000" })) {
      property.set(v[0])
      block()
    }
  }

  // a combo box with any content: typically selectables
  inline fun useCombo(label : String, previewValue : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginCombo(label, previewValue, flags)) {
      block()
      ImGui.EndCombo()
    }
  }

  inline fun listBox(label : String, size : ImVec2 = ImVec2(0.0f, 0.0f), block : () -> Unit) {
    if(ImGui.BeginListBox(label, size)) {
      block()
      ImGui.EndListBox()
    }
  }

  inline fun selectable(label : String, selected : Boolean = false, flags : Int = 0, block : () -> Unit) {
    if(ImGui.Selectable(label, selected, flags)) {
      block()
    }
  }

  // Sliders

  inline fun slider(label : String, property : KMutableProperty0<Float>, min : Float, max : Float, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.SliderFloat(label, v, min, max, format)) {
      property.set(v[0])
      block()
    }
  }

  inline fun slider(label : String, property : KMutableProperty0<Int>, min : Int, max : Int, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.SliderInt(label, v, min, max)) {
      property.set(v[0])
      block()
    }
  }

  // edits the vector in place
  inline fun slider2(label : String, vector : Vec2, min : Float, max : Float, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(vector.x, vector.y)
    if(ImGui.SliderFloat2(label, v, min, max, format)) {
      vector.set(v[0], v[1])
      block()
    }
  }

  inline fun slider3(label : String, vector : Vec3, min : Float, max : Float, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(vector.x, vector.y, vector.z)
    if(ImGui.SliderFloat3(label, v, min, max, format)) {
      vector.set(v[0], v[1], v[2])
      block()
    }
  }

  // Drags: the value changes by `speed` for every pixel the mouse moves. No limits if min and max are the same.

  inline fun dragFloat(label : String, property : KMutableProperty0<Float>, speed : Float = 1.0f, min : Float = 0.0f, max : Float = 0.0f, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.DragFloat(label, v, speed, min, max, format)) {
      property.set(v[0])
      block()
    }
  }

  inline fun dragInt(label : String, property : KMutableProperty0<Int>, speed : Float = 1.0f, min : Int = 0, max : Int = 0, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.DragInt(label, v, speed, min, max)) {
      property.set(v[0])
      block()
    }
  }

  inline fun dragFloat2(label : String, vector : Vec2, speed : Float = 1.0f, min : Float = 0.0f, max : Float = 0.0f, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(vector.x, vector.y)
    if(ImGui.DragFloat2(label, v, speed, min, max, format)) {
      vector.set(v[0], v[1])
      block()
    }
  }

  inline fun dragFloat3(label : String, vector : Vec3, speed : Float = 1.0f, min : Float = 0.0f, max : Float = 0.0f, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(vector.x, vector.y, vector.z)
    if(ImGui.DragFloat3(label, v, speed, min, max, format)) {
      vector.set(v[0], v[1], v[2])
      block()
    }
  }

  inline fun dragFloatRange(label : String, lower : KMutableProperty0<Float>, upper : KMutableProperty0<Float>, speed : Float = 1.0f, min : Float = 0.0f, max : Float = 0.0f, format : String = "%.3f", block : () -> Unit = {}) {
    val l = arrayOf(lower.get())
    val u = arrayOf(upper.get())
    if(ImGui.DragFloatRange2(label, l, u, speed, min, max, format)) {
      lower.set(l[0])
      upper.set(u[0])
      block()
    }
  }

  // Typed input

  inline fun inputText(label : String, property : KMutableProperty0<String>, maxLength : Int = 256, flags : Int = 0, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.InputText(label, v, maxLength, flags)) {
      property.set(v[0])
      block()
    }
  }

  inline fun inputInt(label : String, property : KMutableProperty0<Int>, step : Int = 1, stepFast : Int = 100, block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.InputInt(label, v, step, stepFast)) {
      property.set(v[0])
      block()
    }
  }

  inline fun inputFloat(label : String, property : KMutableProperty0<Float>, step : Float = 0.0f, stepFast : Float = 0.0f, format : String = "%.3f", block : () -> Unit = {}) {
    val v = arrayOf(property.get())
    if(ImGui.InputFloat(label, v, step, stepFast, format)) {
      property.set(v[0])
      block()
    }
  }

  // Colors

  // edits the vector in place
  inline fun colorEdit3(label : String, color : Vec3, flags : Int = 0, block : () -> Unit = {}) {
    val v = arrayOf(color.x, color.y, color.z)
    if(ImGui.ColorEdit3(label, v, flags)) {
      color.set(v[0], v[1], v[2])
      block()
    }
  }

  inline fun colorEdit4(label : String, color : Vec4, flags : Int = 0, block : () -> Unit = {}) {
    val v = arrayOf(color.x, color.y, color.z, color.w)
    if(ImGui.ColorEdit4(label, v, flags)) {
      color.set(v[0], v[1], v[2], v[3])
      block()
    }
  }

  // Trees: the block is what is inside, shown when the node is open

  inline fun treeNode(label : String, block : () -> Unit) {
    if(ImGui.TreeNode(label)) {
      block()
      ImGui.TreePop()
    }
  }

  // flags are ImGuiTreeNodeFlags
  inline fun treeNodeEx(label : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.TreeNodeEx(label, flags)) {
      block()
      // a node with NoTreePushOnOpen is not popped
      if(flags and ImGuiTreeNodeFlags.NoTreePushOnOpen == 0) {
        ImGui.TreePop()
      }
    }
  }

  inline fun collapsingHeader(label : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.CollapsingHeader(label, flags)) {
      block()
    }
  }

  // a header with a close button: `visible` is set to false when it is clicked
  inline fun collapsingHeader(label : String, visible : KMutableProperty0<Boolean>, flags : Int = 0, block : () -> Unit) {
    if(!visible.get()) return
    val v = arrayOf(true)
    if(ImGui.CollapsingHeaderBoolPtr(label, v, flags)) {
      block()
    }
    if(!v[0]) visible.set(false)
  }

  // Menus

  // at the top of the screen
  inline fun mainMenuBar(block : () -> Unit) {
    if(ImGui.BeginMainMenuBar()) {
      block()
      ImGui.EndMainMenuBar()
    }
  }

  // at the top of a window that has ImGuiWindowFlags.MenuBar
  inline fun menuBar(block : () -> Unit) {
    if(ImGui.BeginMenuBar()) {
      block()
      ImGui.EndMenuBar()
    }
  }

  inline fun menu(label : String, enabled : Boolean = true, block : () -> Unit) {
    if(ImGui.BeginMenu(label, enabled)) {
      block()
      ImGui.EndMenu()
    }
  }

  inline fun menuItem(label : String, shortcut : String = "", selected : Boolean = false, enabled : Boolean = true, block : () -> Unit) {
    if(ImGui.MenuItem(label, shortcut, selected, enabled)) {
      block()
    }
  }

  // Popups: opened by openPopup with the same id, or by a click for the context ones

  inline fun popup(strId : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginPopup(strId, flags)) {
      block()
      ImGui.EndPopup()
    }
  }

  // nothing else reacts until it is closed
  inline fun popupModal(name : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginPopupModal(name, null, flags)) {
      block()
      ImGui.EndPopup()
    }
  }

  // on a right click on the widget made just before
  inline fun popupContextItem(block : () -> Unit) {
    if(ImGui.BeginPopupContextItem()) {
      block()
      ImGui.EndPopup()
    }
  }

  // on a right click in the window
  inline fun popupContextWindow(block : () -> Unit) {
    if(ImGui.BeginPopupContextWindow()) {
      block()
      ImGui.EndPopup()
    }
  }

  // on a right click where there is no window
  inline fun popupContextVoid(block : () -> Unit) {
    if(ImGui.BeginPopupContextVoid()) {
      block()
      ImGui.EndPopup()
    }
  }

  fun openPopup(strId : String) {
    ImGui.OpenPopup(strId)
  }

  fun closeCurrentPopup() {
    ImGui.CloseCurrentPopup()
  }

  // Tables: table { row { column { ... } column { ... } } }

  // flags are ImGuiTableFlags
  inline fun table(strId : String, columns : Int, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginTable(strId, columns, flags)) {
      block()
      ImGui.EndTable()
    }
  }

  // a row of column titles, before the first row
  fun tableHeaders(vararg labels : String) {
    for(label in labels) {
      ImGui.TableSetupColumn(label)
    }
    ImGui.TableHeadersRow()
  }

  inline fun row(block : () -> Unit) {
    ImGui.TableNextRow()
    block()
  }

  inline fun column(block : () -> Unit) {
    ImGui.TableNextColumn()
    block()
  }

  // Tabs: tabBar { tabItem("one") { ... } tabItem("two") { ... } }

  inline fun tabBar(strId : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginTabBar(strId, flags)) {
      block()
      ImGui.EndTabBar()
    }
  }

  inline fun tabItem(label : String, flags : Int = 0, block : () -> Unit) {
    if(ImGui.BeginTabItem(label, null, flags)) {
      block()
      ImGui.EndTabItem()
    }
  }
}
