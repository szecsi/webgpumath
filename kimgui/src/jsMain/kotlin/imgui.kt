@file:JsModule("@mori2003/jsimgui")
@file:JsNonModule

package vision.gears.kimgui

import kotlin.js.Promise
import org.khronos.webgl.Uint8Array
import vision.gears.webgpumath.GPURenderPassEncoder

// Kotlin declarations for jsimgui, the JavaScript bindings of Dear ImGui.
// Names and parameters are those of build/imgui.d.ts in the npm package; what is
// missing here can be added from there. Flags and enumerations are in flags.kt.
//
// A value that a widget may change is passed in an array, which the widget
// writes back into. The functions of dsl.kt do that for Kotlin properties.
//
// Not declared: images (ImGui.Image), drag and drop, draw lists.

external object ImGuiImplWeb {
  // options: canvas, device (for WebGPU)
  fun Init(options : dynamic) : Promise<Unit>
  fun BeginRender()
  // draws the GUI into a render pass without a depth buffer
  fun EndRender(passEncoder : GPURenderPassEncoder)
  // makes the bytes of a font file available under the name, for ImGui.GetIO().Fonts.AddFontFromFileTTF
  fun LoadFont(filename : String, fontData : Uint8Array)
}

external class ImVec2(x : Float, y : Float) {
  var x : Float
  var y : Float
}

external class ImVec4(x : Float, y : Float, z : Float, w : Float) {
  var x : Float
  var y : Float
  var z : Float
  var w : Float
}

external class ImFont

external class ImFontAtlas {
  fun AddFontDefault() : ImFont
  // the first font added is the default one; glyphs are made when first used, in any size
  fun AddFontFromFileTTF(filename : String, size_pixels : Float = definedExternally) : ImFont
}

external class ImGuiIO {
  val Fonts : ImFontAtlas
  var FontDefault : ImFont
  val DisplaySize : ImVec2
  val DeltaTime : Float
  val Framerate : Float
  // true when the mouse is over the GUI, or a text field is being typed into: the app should ignore the input then
  val WantCaptureMouse : Boolean
  val WantCaptureKeyboard : Boolean
  val MousePos : ImVec2
  val MouseDelta : ImVec2
  val KeyCtrl : Boolean
  val KeyShift : Boolean
  val KeyAlt : Boolean
  val KeySuper : Boolean
}

external class ImGuiStyle {
  var FontSizeBase : Float   // size of text, before scaling
  var FontScaleMain : Float  // scales all text
  var Alpha : Float
  var WindowRounding : Float
}

external object ImGui {
  fun GetIO() : ImGuiIO
  fun GetStyle() : ImGuiStyle
  fun GetVersion() : String
  fun GetTime() : Double
  fun StyleColorsDark()
  fun StyleColorsLight()
  fun StyleColorsClassic()
  fun ShowDemoWindow(p_open : Array<Boolean>? = definedExternally)

  // Windows
  fun Begin(name : String, p_open : Array<Boolean>? = definedExternally, flags : Int = definedExternally) : Boolean
  fun End()
  fun BeginChild(str_id : String, size : ImVec2 = definedExternally, child_flags : Int = definedExternally, window_flags : Int = definedExternally) : Boolean
  fun EndChild()
  fun IsWindowAppearing() : Boolean
  fun IsWindowFocused(flags : Int = definedExternally) : Boolean
  fun IsWindowHovered(flags : Int = definedExternally) : Boolean
  fun GetWindowPos() : ImVec2
  fun GetWindowSize() : ImVec2
  fun GetWindowWidth() : Float
  fun GetWindowHeight() : Float
  fun SetNextWindowPos(pos : ImVec2, cond : Int = definedExternally, pivot : ImVec2 = definedExternally)
  fun SetNextWindowSize(size : ImVec2, cond : Int = definedExternally)
  fun SetNextWindowSizeConstraints(min : ImVec2, max : ImVec2)
  fun SetNextWindowCollapsed(collapsed : Boolean, cond : Int = definedExternally)
  fun SetNextWindowFocus()
  fun SetNextWindowBgAlpha(alpha : Float)
  fun SetWindowFocus()

  // Style and font stacks
  fun PushFontFloat(font : ImFont?, font_size_base_unscaled : Float)
  fun PopFont()
  fun GetFont() : ImFont
  fun GetFontSize() : Float
  fun PushStyleColorImVec4(idx : Int, col : ImVec4)
  fun PopStyleColor(count : Int = definedExternally)
  fun PushStyleVar(idx : Int, value : Float)
  fun PushStyleVarImVec2(idx : Int, value : ImVec2)
  fun PopStyleVar(count : Int = definedExternally)
  fun PushItemFlag(option : Int, enabled : Boolean)
  fun PopItemFlag()
  fun PushItemWidth(item_width : Float)
  fun PopItemWidth()
  fun SetNextItemWidth(item_width : Float)
  fun PushTextWrapPos(wrap_local_pos_x : Float = definedExternally)
  fun PopTextWrapPos()
  fun PushClipRect(clip_rect_min : ImVec2, clip_rect_max : ImVec2, intersect_with_current_clip_rect : Boolean)
  fun PopClipRect()
  fun BeginDisabled(disabled : Boolean = definedExternally)
  fun EndDisabled()

  // Layout
  fun Separator()
  fun SeparatorText(label : String)
  fun SameLine(offset_from_start_x : Float = definedExternally, spacing : Float = definedExternally)
  fun NewLine()
  fun Spacing()
  fun Dummy(size : ImVec2)
  fun Indent(indent_w : Float = definedExternally)
  fun Unindent(indent_w : Float = definedExternally)
  fun BeginGroup()
  fun EndGroup()
  fun AlignTextToFramePadding()
  fun GetCursorScreenPos() : ImVec2
  fun SetCursorScreenPos(pos : ImVec2)
  fun GetCursorPos() : ImVec2
  fun GetCursorPosX() : Float
  fun GetCursorPosY() : Float
  fun SetCursorPos(local_pos : ImVec2)
  fun SetCursorPosX(local_x : Float)
  fun SetCursorPosY(local_y : Float)
  fun GetContentRegionAvail() : ImVec2
  fun GetTextLineHeight() : Float
  fun GetFrameHeight() : Float
  fun CalcTextSize(text : String) : ImVec2

  // Ids: widgets with the same label need different ids
  fun PushID(str_id : String)
  fun PushIDInt(int_id : Int)
  fun PopID()

  // Text
  fun Text(text : String)
  fun TextColored(col : ImVec4, text : String)
  fun TextDisabled(text : String)
  fun TextWrapped(text : String)
  fun LabelText(label : String, text : String)
  fun BulletText(text : String)
  fun Bullet()

  // Buttons and the like
  fun Button(label : String, size : ImVec2 = definedExternally) : Boolean
  fun SmallButton(label : String) : Boolean
  fun InvisibleButton(str_id : String, size : ImVec2, flags : Int = definedExternally) : Boolean
  fun ArrowButton(str_id : String, dir : Int) : Boolean
  fun Checkbox(label : String, v : Array<Boolean>) : Boolean
  fun CheckboxFlagsIntPtr(label : String, flags : Array<Int>, flags_value : Int) : Boolean
  fun RadioButton(label : String, active : Boolean) : Boolean
  fun RadioButtonIntPtr(label : String, v : Array<Int>, v_button : Int) : Boolean
  fun ProgressBar(fraction : Float, size_arg : ImVec2 = definedExternally, overlay : String = definedExternally)
  fun ColorButton(desc_id : String, col : ImVec4, flags : Int = definedExternally, size : ImVec2 = definedExternally) : Boolean

  // Combo and list boxes
  fun BeginCombo(label : String, preview_value : String, flags : Int = definedExternally) : Boolean
  fun EndCombo()
  fun Combo(label : String, current_item : Array<Int>, items_separated_by_zeros : String, popup_max_height_in_items : Int = definedExternally) : Boolean
  fun BeginListBox(label : String, size : ImVec2 = definedExternally) : Boolean
  fun EndListBox()
  fun Selectable(label : String, selected : Boolean = definedExternally, flags : Int = definedExternally, size : ImVec2 = definedExternally) : Boolean

  // Drags, sliders, inputs
  fun DragFloat(label : String, v : Array<Float>, v_speed : Float = definedExternally, v_min : Float = definedExternally, v_max : Float = definedExternally, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun DragFloat2(label : String, v : Array<Float>, v_speed : Float = definedExternally, v_min : Float = definedExternally, v_max : Float = definedExternally, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun DragFloat3(label : String, v : Array<Float>, v_speed : Float = definedExternally, v_min : Float = definedExternally, v_max : Float = definedExternally, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun DragFloatRange2(label : String, v_current_min : Array<Float>, v_current_max : Array<Float>, v_speed : Float = definedExternally, v_min : Float = definedExternally, v_max : Float = definedExternally, format : String = definedExternally, format_max : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun DragInt(label : String, v : Array<Int>, v_speed : Float = definedExternally, v_min : Int = definedExternally, v_max : Int = definedExternally, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun SliderFloat(label : String, v : Array<Float>, v_min : Float, v_max : Float, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun SliderFloat2(label : String, v : Array<Float>, v_min : Float, v_max : Float, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun SliderFloat3(label : String, v : Array<Float>, v_min : Float, v_max : Float, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun SliderInt(label : String, v : Array<Int>, v_min : Int, v_max : Int, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun InputText(label : String, buf : Array<String>, buf_size : Int, flags : Int = definedExternally) : Boolean
  fun InputTextWithHint(label : String, hint : String, buf : Array<String>, buf_size : Int, flags : Int = definedExternally) : Boolean
  fun InputFloat(label : String, v : Array<Float>, step : Float = definedExternally, step_fast : Float = definedExternally, format : String = definedExternally, flags : Int = definedExternally) : Boolean
  fun InputInt(label : String, v : Array<Int>, step : Int = definedExternally, step_fast : Int = definedExternally, flags : Int = definedExternally) : Boolean
  fun ColorEdit3(label : String, col : Array<Float>, flags : Int = definedExternally) : Boolean
  fun ColorEdit4(label : String, col : Array<Float>, flags : Int = definedExternally) : Boolean
  fun PlotLines(label : String, values : Array<Float>, values_count : Int, values_offset : Int = definedExternally, overlay_text : String = definedExternally, scale_min : Float = definedExternally, scale_max : Float = definedExternally, graph_size : ImVec2 = definedExternally)
  fun PlotHistogram(label : String, values : Array<Float>, values_count : Int, values_offset : Int = definedExternally, overlay_text : String = definedExternally, scale_min : Float = definedExternally, scale_max : Float = definedExternally, graph_size : ImVec2 = definedExternally)

  // Trees
  fun TreeNode(label : String) : Boolean
  fun TreeNodeEx(label : String, flags : Int = definedExternally) : Boolean
  fun TreePop()
  fun CollapsingHeader(label : String, flags : Int = definedExternally) : Boolean
  fun CollapsingHeaderBoolPtr(label : String, p_visible : Array<Boolean>, flags : Int = definedExternally) : Boolean

  // Menus
  fun BeginMainMenuBar() : Boolean
  fun EndMainMenuBar()
  fun BeginMenuBar() : Boolean
  fun EndMenuBar()
  fun BeginMenu(label : String, enabled : Boolean = definedExternally) : Boolean
  fun EndMenu()
  fun MenuItem(label : String, shortcut : String = definedExternally, selected : Boolean = definedExternally, enabled : Boolean = definedExternally) : Boolean

  // Tooltips and popups
  fun BeginTooltip() : Boolean
  fun EndTooltip()
  fun SetTooltip(text : String)
  fun SetItemTooltip(text : String)
  fun BeginPopup(str_id : String, flags : Int = definedExternally) : Boolean
  fun BeginPopupModal(name : String, p_open : Array<Boolean>? = definedExternally, flags : Int = definedExternally) : Boolean
  fun EndPopup()
  fun OpenPopup(str_id : String, popup_flags : Int = definedExternally)
  fun CloseCurrentPopup()
  fun BeginPopupContextItem(str_id : String = definedExternally, popup_flags : Int = definedExternally) : Boolean
  fun BeginPopupContextWindow(str_id : String = definedExternally, popup_flags : Int = definedExternally) : Boolean
  fun BeginPopupContextVoid(str_id : String = definedExternally, popup_flags : Int = definedExternally) : Boolean
  fun IsPopupOpen(str_id : String, flags : Int = definedExternally) : Boolean

  // Tables
  fun BeginTable(str_id : String, columns : Int, flags : Int = definedExternally, outer_size : ImVec2 = definedExternally, inner_width : Float = definedExternally) : Boolean
  fun EndTable()
  fun TableSetupColumn(label : String, flags : Int = definedExternally, init_width_or_weight : Float = definedExternally)
  fun TableHeadersRow()
  fun TableNextRow(row_flags : Int = definedExternally, min_row_height : Float = definedExternally)
  fun TableNextColumn() : Boolean
  fun TableSetColumnIndex(column_n : Int) : Boolean

  // Tabs
  fun BeginTabBar(str_id : String, flags : Int = definedExternally) : Boolean
  fun EndTabBar()
  fun BeginTabItem(label : String, p_open : Array<Boolean>? = definedExternally, flags : Int = definedExternally) : Boolean
  fun EndTabItem()

  // About the widget that was just made
  fun IsItemHovered(flags : Int = definedExternally) : Boolean
  fun IsItemActive() : Boolean
  fun IsItemClicked(mouse_button : Int = definedExternally) : Boolean
  fun IsItemEdited() : Boolean
  fun IsItemDeactivatedAfterEdit() : Boolean
  fun GetItemRectSize() : ImVec2
  fun SetItemDefaultFocus()
  fun SetKeyboardFocusHere(offset : Int = definedExternally)
  fun SetNextItemAllowOverlap()

  // Input
  fun IsKeyDown(key : Int) : Boolean
  fun IsMouseDown(button : Int) : Boolean
  fun GetMouseCursor() : Int
  fun SetMouseCursor(cursor_type : Int)
}
