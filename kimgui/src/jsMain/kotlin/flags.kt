@file:JsModule("@mori2003/jsimgui")
@file:JsNonModule

package vision.gears.kimgui

// The flags and enumerations of Dear ImGui, as jsimgui exports them.
// Generated from build/imgui.d.ts of @mori2003/jsimgui 0.14.0. Do not edit: regenerate.
//
// All values are Ints. Flags are combined with `or`:
//   ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize

external object ImGuiWindowFlags {
  val None : Int
  val NoTitleBar : Int  // Disable title-bar
  val NoResize : Int  // Disable user resizing with the lower-right grip
  val NoMove : Int  // Disable user moving the window
  val NoScrollbar : Int  // Disable scrollbars (window can still scroll with mouse or programmatically)
  val NoScrollWithMouse : Int  // Disable user vertically scrolling with mouse wheel. On child window, mouse wheel will be forwarded to the parent unless NoScrollbar is also set.
  val NoCollapse : Int  // Disable user collapsing window by double-clicking on it. Also referred to as Window Menu Button (e.g. within a docking node).
  val AlwaysAutoResize : Int  // Resize every window to its content every frame
  val NoBackground : Int  // Disable drawing background color (WindowBg, etc.) and outside border. Similar as using SetNextWindowBgAlpha(0.0f).
  val NoSavedSettings : Int  // Never load/save settings in .ini file
  val NoMouseInputs : Int  // Disable catching mouse, hovering test with pass through.
  val MenuBar : Int  // Has a menu-bar
  val HorizontalScrollbar : Int  // Allow horizontal scrollbar to appear (off by default). You may use SetNextWindowContentSize(ImVec2(width,0.0f)); prior to calling Begin() to specify width. Read code in imgui_demo in the "Horizontal Scrolling" section.
  val NoFocusOnAppearing : Int  // Disable taking focus when transitioning from hidden to visible state
  val NoBringToFrontOnFocus : Int  // Disable bringing window to front when taking focus (e.g. clicking on it or programmatically giving it focus)
  val AlwaysVerticalScrollbar : Int  // Always show vertical scrollbar (even if ContentSize.y < Size.y)
  val AlwaysHorizontalScrollbar : Int  // Always show horizontal scrollbar (even if ContentSize.x < Size.x)
  val NoNavInputs : Int  // No keyboard/gamepad navigation within the window
  val NoNavFocus : Int  // No focusing toward this window with keyboard/gamepad navigation (e.g. skipped by Ctrl+Tab)
  val UnsavedDocument : Int  // Display a dot next to the title. When used in a tab/docking context, tab is selected when clicking the X + closure is not assumed (will wait for user to stop submitting the tab). Otherwise closure is assumed when pressing the X, so if you keep submitting the tab may reappear at end of tab bar.
  val NoDocking : Int  // Disable docking of this window
  val NoNav : Int
  val NoDecoration : Int
  val NoInputs : Int
}

external object ImGuiChildFlags {
  val None : Int
  val Borders : Int  // Show an outer border and enable WindowPadding. (IMPORTANT: this is always == 1 == true for legacy reason)
  val AlwaysUseWindowPadding : Int  // Pad with style.WindowPadding even if no border are drawn (no padding by default for non-bordered child windows because it makes more sense)
  val ResizeX : Int  // Allow resize from right border (layout direction). Enable .ini saving (unless ImGuiWindowFlags_NoSavedSettings passed to window flags)
  val ResizeY : Int  // Allow resize from bottom border (layout direction). "
  val AutoResizeX : Int  // Enable auto-resizing width. Read "IMPORTANT: Size measurement" details above.
  val AutoResizeY : Int  // Enable auto-resizing height. Read "IMPORTANT: Size measurement" details above.
  val AlwaysAutoResize : Int  // Combined with AutoResizeX/AutoResizeY. Always measure size even when child is hidden, always return true, always disable clipping optimization! NOT RECOMMENDED.
  val FrameStyle : Int  // Style the child window like a framed item: use FrameBg, FrameRounding, FrameBorderSize, FramePadding instead of ChildBg, ChildRounding, ChildBorderSize, WindowPadding.
  val NavFlattened : Int  // [BETA] Share focus scope, allow keyboard/gamepad navigation to cross over parent border to this child or between sibling child windows.
}

external object ImGuiItemFlags {
  val None : Int  // (Default)
  val NoTabStop : Int  // false // Disable keyboard tabbing. This is a "lighter" version of ImGuiItemFlags_NoNav.
  val NoNav : Int  // false // Disable any form of focusing (keyboard/gamepad directional navigation and SetKeyboardFocusHere() calls).
  val NoNavDefaultFocus : Int  // false // Disable item being a candidate for default focus (e.g. used by title bar items).
  val ButtonRepeat : Int  // false // Any button-like behavior will have repeat mode enabled (based on io.KeyRepeatDelay and io.KeyRepeatRate values). Note that you can also call IsItemActive() after any button to tell if it is being held.
  val AutoClosePopups : Int  // true // MenuItem()/Selectable() automatically close their parent popup window.
  val AllowDuplicateId : Int  // false // Allow submitting an item with the same identifier as an item already submitted this frame without triggering a warning tooltip if io.ConfigDebugHighlightIdConflicts is set.
  val Disabled : Int  // false // [Internal] Disable interactions. DOES NOT affect visuals. This is used by BeginDisabled()/EndDisabled() and only provided here so you can read back via GetItemFlags().
}

external object ImGuiInputTextFlags {
  val None : Int  // Basic filters (also see ImGuiInputTextFlags_CallbackCharFilter)
  val CharsDecimal : Int  // Allow 0123456789.+-* /
  val CharsHexadecimal : Int  // Allow 0123456789ABCDEFabcdef
  val CharsScientific : Int  // Allow 0123456789.+-* /eE (Scientific notation input)
  val CharsUppercase : Int  // Turn a..z into A..Z
  val CharsNoBlank : Int  // Filter out spaces, tabs
  val AllowTabInput : Int  // Pressing TAB input a '\t' character into the text field
  val EnterReturnsTrue : Int  // Return 'true' when Enter is pressed (as opposed to every time the value was modified). Consider using IsItemDeactivatedAfterEdit() instead!
  val EscapeClearsAll : Int  // Escape key clears content if not empty, and deactivate otherwise (contrast to default behavior of Escape to revert)
  val CtrlEnterForNewLine : Int  // In multi-line mode: validate with Enter, add new line with Ctrl+Enter (default is opposite: validate with Ctrl+Enter, add line with Enter). Note that Shift+Enter always enter a new line either way.
  val ReadOnly : Int  // Read-only mode
  val Password : Int  // Password mode, display all characters as '*', disable copy
  val AlwaysOverwrite : Int  // Overwrite mode
  val AutoSelectAll : Int  // Select entire text when first taking mouse focus
  val ParseEmptyRefVal : Int  // InputFloat(), InputInt(), InputScalar() etc. only: parse empty string as zero value.
  val DisplayEmptyRefVal : Int  // InputFloat(), InputInt(), InputScalar() etc. only: when value is zero, do not display it. Generally used with ImGuiInputTextFlags_ParseEmptyRefVal.
  val NoHorizontalScroll : Int  // Disable following the cursor horizontally
  val NoUndoRedo : Int  // Disable undo/redo. Note that input text owns the text data while active, if you want to provide your own undo/redo stack you need e.g. to call ClearActiveID().
  val ElideLeft : Int  // When text doesn't fit, elide left side to ensure right side stays visible. Useful for path/filenames. Single-line only!
  val CallbackCompletion : Int  // Callback on pressing TAB (for completion handling)
  val CallbackHistory : Int  // Callback on pressing Up/Down arrows (for history handling)
  val CallbackAlways : Int  // Callback on each iteration. User code may query cursor position, modify text buffer.
  val CallbackCharFilter : Int  // Callback on character inputs to replace or discard them. Modify 'EventChar' to replace or discard, or return 1 in callback to discard.
  val CallbackResize : Int  // Callback on buffer capacity changes request (beyond 'buf_size' parameter value), allowing the string to grow. Notify when the string wants to be resized (for string types which hold a cache of their Size). You will be provided a new BufSize in the callback and NEED to honor it. (see misc/cpp/imgui_stdlib.h for an example of using this)
  val CallbackEdit : Int  // Callback on any edit. Note that InputText() already returns true on edit + you can always use IsItemEdited(). The callback is useful to manipulate the underlying buffer while focus is active.
  val WordWrap : Int  // InputTextMultiline(): word-wrap lines that are too long.
}

external object ImGuiTreeNodeFlags {
  val None : Int
  val Selected : Int  // Draw as selected
  val Framed : Int  // Draw frame with background (e.g. for CollapsingHeader)
  val AllowOverlap : Int  // Hit testing will allow subsequent widgets to overlap this one. Require previous frame HoveredId to match before being usable. Shortcut to calling SetNextItemAllowOverlap().
  val NoTreePushOnOpen : Int  // Don't do a TreePush() when open (e.g. for CollapsingHeader) = no extra indent nor pushing on ID stack
  val NoAutoOpenOnLog : Int  // Don't automatically and temporarily open node when Logging is active (by default logging will automatically open tree nodes)
  val DefaultOpen : Int  // Default node to be open
  val OpenOnDoubleClick : Int  // Open on double-click instead of simple click (default for multi-select unless any _OpenOnXXX behavior is set explicitly). Both behaviors may be combined.
  val OpenOnArrow : Int  // Open when clicking on the arrow part (default for multi-select unless any _OpenOnXXX behavior is set explicitly). Both behaviors may be combined.
  val Leaf : Int  // No collapsing, no arrow (use as a convenience for leaf nodes). Note: will always open a tree/id scope and return true. If you never use that scope, add ImGuiTreeNodeFlags_NoTreePushOnOpen.
  val Bullet : Int  // Display a bullet instead of arrow. IMPORTANT: node can still be marked open/close if you don't set the _Leaf flag!
  val FramePadding : Int  // Use FramePadding (even for an unframed text node) to vertically align text baseline to regular widget height. Equivalent to calling AlignTextToFramePadding() before the node.
  val SpanAvailWidth : Int  // Extend hit box to the right-most edge, even if not framed. This is not the default in order to allow adding other items on the same line without using AllowOverlap mode.
  val SpanFullWidth : Int  // Extend hit box to the left-most and right-most edges (cover the indent area).
  val SpanLabelWidth : Int  // Narrow hit box + narrow hovering highlight, will only cover the label text.
  val SpanAllColumns : Int  // Frame will span all columns of its container table (label will still fit in current column)
  val LabelSpanAllColumns : Int  // Label will span all columns of its container table
  val NavLeftJumpsToParent : Int  // Nav: left arrow moves back to parent. This is processed in TreePop() when there's an unfulfilled Left nav request remaining.
  val CollapsingHeader : Int
  val DrawLinesNone : Int  // No lines drawn
  val DrawLinesFull : Int  // Horizontal lines to child nodes. Vertical line drawn down to TreePop() position: cover full contents. Faster (for large trees).
  val DrawLinesToNodes : Int  // Horizontal lines to child nodes. Vertical line drawn down to bottom-most child node. Slower (for large trees).
}

external object ImGuiPopupFlags {
  val None : Int
  val MouseButtonLeft : Int  // For BeginPopupContext*(): open on Left Mouse release. Only one button allowed!
  val MouseButtonRight : Int  // For BeginPopupContext*(): open on Right Mouse release. Only one button allowed! (default)
  val MouseButtonMiddle : Int  // For BeginPopupContext*(): open on Middle Mouse release. Only one button allowed!
  val NoReopen : Int  // For OpenPopup*(), BeginPopupContext*(): don't reopen same popup if already open (won't reposition, won't reinitialize navigation)
  val NoOpenOverExistingPopup : Int  // For OpenPopup*(), BeginPopupContext*(): don't open if there's already a popup at the same level of the popup stack
  val NoOpenOverItems : Int  // For BeginPopupContextWindow(): don't return true when hovering items, only when hovering empty space
  val AnyPopupId : Int  // For IsPopupOpen(): ignore the ImGuiID parameter and test for any popup.
  val AnyPopupLevel : Int  // For IsPopupOpen(): search/test at any level of the popup stack (default test in the current level)
  val AnyPopup : Int
}

external object ImGuiSelectableFlags {
  val None : Int
  val NoAutoClosePopups : Int  // Clicking this doesn't close parent popup window (overrides ImGuiItemFlags_AutoClosePopups)
  val SpanAllColumns : Int  // Frame will span all columns of its container table (text will still fit in current column)
  val AllowDoubleClick : Int  // Generate press events on double clicks too
  val Disabled : Int  // Cannot be selected, display grayed out text
  val AllowOverlap : Int  // Hit testing will allow subsequent widgets to overlap this one. Require previous frame HoveredId to match before being usable. Shortcut to calling SetNextItemAllowOverlap().
  val Highlight : Int  // Make the item be displayed as if it is hovered
  val SelectOnNav : Int  // Auto-select when moved into, unless Ctrl is held. Automatic when in a BeginMultiSelect() block.
}

external object ImGuiComboFlags {
  val None : Int
  val PopupAlignLeft : Int  // Align the popup toward the left by default
  val HeightSmall : Int  // Max ~4 items visible. Tip: If you want your combo popup to be a specific size you can use SetNextWindowSizeConstraints() prior to calling BeginCombo()
  val HeightRegular : Int  // Max ~8 items visible (default)
  val HeightLarge : Int  // Max ~20 items visible
  val HeightLargest : Int  // As many fitting items as possible
  val NoArrowButton : Int  // Display on the preview box without the square arrow button
  val NoPreview : Int  // Display only a square arrow button
  val WidthFitPreview : Int  // Width dynamically calculated from preview contents
}

external object ImGuiTabBarFlags {
  val None : Int
  val Reorderable : Int  // Allow manually dragging tabs to re-order them + New tabs are appended at the end of list
  val AutoSelectNewTabs : Int  // Automatically select new tabs when they appear
  val TabListPopupButton : Int  // Disable buttons to open the tab list popup
  val NoCloseWithMiddleMouseButton : Int  // Disable behavior of closing tabs (that are submitted with p_open != NULL) with middle mouse button. You may handle this behavior manually on user's side with if (IsItemHovered() && IsMouseClicked(2)) *p_open = false.
  val NoTabListScrollingButtons : Int  // Disable scrolling buttons (apply when fitting policy is ImGuiTabBarFlags_FittingPolicyScroll)
  val NoTooltip : Int  // Disable tooltips when hovering a tab
  val DrawSelectedOverline : Int  // Draw selected overline markers over selected tab
  val FittingPolicyMixed : Int  // Shrink down tabs when they don't fit, until width is style.TabMinWidthShrink, then enable scrolling. Setting TabMinWidthShrink to FLT_MAX makes this behave like ImGuiTabBarFlags_FittingPolicyScroll.
  val FittingPolicyShrink : Int  // Shrink down tabs when they don't fit
  val FittingPolicyScroll : Int  // Enable scrolling buttons when tabs don't fit
}

external object ImGuiTabItemFlags {
  val None : Int
  val UnsavedDocument : Int  // Display a dot next to the title + set ImGuiTabItemFlags_NoAssumedClosure.
  val SetSelected : Int  // Trigger flag to programmatically make the tab selected when calling BeginTabItem()
  val NoCloseWithMiddleMouseButton : Int  // Disable behavior of closing tabs (that are submitted with p_open != NULL) with middle mouse button. You may handle this behavior manually on user's side with if (IsItemHovered() && IsMouseClicked(2)) *p_open = false.
  val NoPushId : Int  // Don't call PushID()/PopID() on BeginTabItem()/EndTabItem()
  val NoTooltip : Int  // Disable tooltip for the given tab
  val NoReorder : Int  // Disable reordering this tab or having another tab cross over this tab
  val Leading : Int  // Enforce the tab position to the left of the tab bar (after the tab list popup button)
  val Trailing : Int  // Enforce the tab position to the right of the tab bar (before the scrolling buttons)
  val NoAssumedClosure : Int  // Tab is selected when trying to close + closure is not immediately assumed (will wait for user to stop submitting the tab). Otherwise closure is assumed when pressing the X, so if you keep submitting the tab may reappear at end of tab bar.
}

external object ImGuiFocusedFlags {
  val None : Int
  val ChildWindows : Int  // Return true if any children of the window is focused
  val RootWindow : Int  // Test from root window (top most parent of the current hierarchy)
  val AnyWindow : Int  // Return true if any window is focused. Important: If you are trying to tell how to dispatch your low-level inputs, do NOT use this. Use 'io.WantCaptureMouse' instead! Please read the FAQ!
  val NoPopupHierarchy : Int  // Do not consider popup hierarchy (do not treat popup emitter as parent of popup) (when used with _ChildWindows or _RootWindow)
  val DockHierarchy : Int  // Consider docking hierarchy (treat dockspace host as parent of docked window) (when used with _ChildWindows or _RootWindow)
  val RootAndChildWindows : Int
}

external object ImGuiHoveredFlags {
  val None : Int  // Return true if directly over the item/window, not obstructed by another window, not obstructed by an active popup or modal blocking inputs under them.
  val ChildWindows : Int  // IsWindowHovered() only: Return true if any children of the window is hovered
  val RootWindow : Int  // IsWindowHovered() only: Test from root window (top most parent of the current hierarchy)
  val AnyWindow : Int  // IsWindowHovered() only: Return true if any window is hovered
  val NoPopupHierarchy : Int  // IsWindowHovered() only: Do not consider popup hierarchy (do not treat popup emitter as parent of popup) (when used with _ChildWindows or _RootWindow)
  val DockHierarchy : Int  // IsWindowHovered() only: Consider docking hierarchy (treat dockspace host as parent of docked window) (when used with _ChildWindows or _RootWindow)
  val AllowWhenBlockedByPopup : Int  // Return true even if a popup window is normally blocking access to this item/window
  val AllowWhenBlockedByActiveItem : Int  // Return true even if an active item is blocking access to this item/window. Useful for Drag and Drop patterns.
  val AllowWhenOverlappedByItem : Int  // IsItemHovered() only: Return true even if the item uses AllowOverlap mode and is overlapped by another hoverable item.
  val AllowWhenOverlappedByWindow : Int  // IsItemHovered() only: Return true even if the position is obstructed or overlapped by another window.
  val AllowWhenDisabled : Int  // IsItemHovered() only: Return true even if the item is disabled
  val NoNavOverride : Int  // IsItemHovered() only: Disable using keyboard/gamepad navigation state when active, always query mouse
  val AllowWhenOverlapped : Int
  val RectOnly : Int
  val RootAndChildWindows : Int
  val ForTooltip : Int  // Shortcut for standard flags when using IsItemHovered() + SetTooltip() sequence.
  val Stationary : Int  // Require mouse to be stationary for style.HoverStationaryDelay (~0.15 sec) _at least one time_. After this, can move on same item/window. Using the stationary test tends to reduces the need for a long delay.
  val DelayNone : Int  // IsItemHovered() only: Return true immediately (default). As this is the default you generally ignore this.
  val DelayShort : Int  // IsItemHovered() only: Return true after style.HoverDelayShort elapsed (~0.15 sec) (shared between items) + requires mouse to be stationary for style.HoverStationaryDelay (once per item).
  val DelayNormal : Int  // IsItemHovered() only: Return true after style.HoverDelayNormal elapsed (~0.40 sec) (shared between items) + requires mouse to be stationary for style.HoverStationaryDelay (once per item).
  val NoSharedDelay : Int  // IsItemHovered() only: Disable shared delay system where moving from one item to the next keeps the previous timer for a short time (standard for tooltips with long delays)
}

external object ImGuiDockNodeFlags {
  val None : Int
  val KeepAliveOnly : Int  // // Don't display the dockspace node but keep it alive. Windows docked into this dockspace node won't be undocked.
  val NoDockingOverCentralNode : Int  // // Disable docking over the Central Node, which will be always kept empty.
  val PassthruCentralNode : Int  // // Enable passthru dockspace: 1) DockSpace() will render a ImGuiCol_WindowBg background covering everything excepted the Central Node when empty. Meaning the host window should probably use SetNextWindowBgAlpha(0.0f) prior to Begin() when using this. 2) When Central Node is empty: let inputs pass-through + won't display a DockingEmptyBg background. See demo for details.
  val NoDockingSplit : Int  // // Disable other windows/nodes from splitting this node.
  val NoResize : Int  // Saved // Disable resizing node using the splitter/separators. Useful with programmatically setup dockspaces.
  val AutoHideTabBar : Int  // // Tab bar will automatically hide when there is a single window in the dock node.
  val NoUndocking : Int  // // Disable undocking this node.
}

external object ImGuiDragDropFlags {
  val None : Int
  val SourceNoPreviewTooltip : Int  // Disable preview tooltip. By default, a successful call to BeginDragDropSource opens a tooltip so you can display a preview or description of the source contents. This flag disables this behavior.
  val SourceNoDisableHover : Int  // By default, when dragging we clear data so that IsItemHovered() will return false, to avoid subsequent user code submitting tooltips. This flag disables this behavior so you can still call IsItemHovered() on the source item.
  val SourceNoHoldToOpenOthers : Int  // Disable the behavior that allows to open tree nodes and collapsing header by holding over them while dragging a source item.
  val SourceAllowNullID : Int  // Allow items such as Text(), Image() that have no unique identifier to be used as drag source, by manufacturing a temporary identifier based on their window-relative position. This is extremely unusual within the dear imgui ecosystem and so we made it explicit.
  val SourceExtern : Int  // External source (from outside of dear imgui), won't attempt to read current item/window info. Will always return true. Only one Extern source can be active simultaneously.
  val PayloadAutoExpire : Int  // Automatically expire the payload if the source cease to be submitted (otherwise payloads are persisting while being dragged)
  val PayloadNoCrossContext : Int  // Hint to specify that the payload may not be copied outside current dear imgui context.
  val PayloadNoCrossProcess : Int  // Hint to specify that the payload may not be copied outside current process.
  val AcceptBeforeDelivery : Int  // AcceptDragDropPayload() will returns true even before the mouse button is released. You can then call IsDelivery() to test if the payload needs to be delivered.
  val AcceptNoDrawDefaultRect : Int  // Do not draw the default highlight rectangle when hovering over target.
  val AcceptNoPreviewTooltip : Int  // Request hiding the BeginDragDropSource tooltip from the BeginDragDropTarget site.
  val AcceptDrawAsHovered : Int  // Accepting item will render as if hovered. Useful for e.g. a Button() used as a drop target.
  val AcceptPeekOnly : Int  // For peeking ahead and inspecting the payload before delivery.
}

external object ImGuiDataType {
  val S8 : Int  // signed char / char (with sensible compilers)
  val U8 : Int  // unsigned char
  val S16 : Int  // short
  val U16 : Int  // unsigned short
  val S32 : Int  // int
  val U32 : Int  // unsigned int
  val S64 : Int  // long long / __int64
  val U64 : Int  // unsigned long long / unsigned __int64
  val Float : Int  // float
  val Double : Int  // double
  val Bool : Int  // bool (provided for user convenience, not supported by scalar widgets)
  val String : Int  // char* (provided for user convenience, not supported by scalar widgets)
  val COUNT : Int
}

external object ImGuiDir {
  val _None : Int
  val _Left : Int
  val _Right : Int
  val _Up : Int
  val _Down : Int
  val _COUNT : Int
}

external object ImGuiSortDirection {
  val _None : Int
  val _Ascending : Int  // Ascending = 0->9, A->Z etc.
  val _Descending : Int  // Descending = 9->0, Z->A etc.
}

external object ImGuiKey {
  val _None : Int  // Keyboard
  val _NamedKey_BEGIN : Int  // First valid key value (other than 0)
  val _Tab : Int  // == ImGuiKey_NamedKey_BEGIN
  val _LeftArrow : Int
  val _RightArrow : Int
  val _UpArrow : Int
  val _DownArrow : Int
  val _PageUp : Int
  val _PageDown : Int
  val _Home : Int
  val _End : Int
  val _Insert : Int
  val _Delete : Int
  val _Backspace : Int
  val _Space : Int
  val _Enter : Int
  val _Escape : Int
  val _LeftCtrl : Int
  val _LeftShift : Int
  val _LeftAlt : Int
  val _LeftSuper : Int  // Also see ImGuiMod_Ctrl, ImGuiMod_Shift, ImGuiMod_Alt, ImGuiMod_Super below!
  val _RightCtrl : Int
  val _RightShift : Int
  val _RightAlt : Int
  val _RightSuper : Int
  val _Menu : Int
  val _0 : Int
  val _1 : Int
  val _2 : Int
  val _3 : Int
  val _4 : Int
  val _5 : Int
  val _6 : Int
  val _7 : Int
  val _8 : Int
  val _9 : Int
  val _A : Int
  val _B : Int
  val _C : Int
  val _D : Int
  val _E : Int
  val _F : Int
  val _G : Int
  val _H : Int
  val _I : Int
  val _J : Int
  val _K : Int
  val _L : Int
  val _M : Int
  val _N : Int
  val _O : Int
  val _P : Int
  val _Q : Int
  val _R : Int
  val _S : Int
  val _T : Int
  val _U : Int
  val _V : Int
  val _W : Int
  val _X : Int
  val _Y : Int
  val _Z : Int
  val _F1 : Int
  val _F2 : Int
  val _F3 : Int
  val _F4 : Int
  val _F5 : Int
  val _F6 : Int
  val _F7 : Int
  val _F8 : Int
  val _F9 : Int
  val _F10 : Int
  val _F11 : Int
  val _F12 : Int
  val _F13 : Int
  val _F14 : Int
  val _F15 : Int
  val _F16 : Int
  val _F17 : Int
  val _F18 : Int
  val _F19 : Int
  val _F20 : Int
  val _F21 : Int
  val _F22 : Int
  val _F23 : Int
  val _F24 : Int
  val _Apostrophe : Int  // '
  val _Comma : Int  // ,
  val _Minus : Int  // -
  val _Period : Int  // .
  val _Slash : Int  // /
  val _Semicolon : Int  // ;
  val _Equal : Int  // =
  val _LeftBracket : Int  // [
  val _Backslash : Int  // \ (this text inhibit multiline comment caused by backslash)
  val _RightBracket : Int  // ]
  val _GraveAccent : Int  // `
  val _CapsLock : Int
  val _ScrollLock : Int
  val _NumLock : Int
  val _PrintScreen : Int
  val _Pause : Int
  val _Keypad0 : Int
  val _Keypad1 : Int
  val _Keypad2 : Int
  val _Keypad3 : Int
  val _Keypad4 : Int
  val _Keypad5 : Int
  val _Keypad6 : Int
  val _Keypad7 : Int
  val _Keypad8 : Int
  val _Keypad9 : Int
  val _KeypadDecimal : Int
  val _KeypadDivide : Int
  val _KeypadMultiply : Int
  val _KeypadSubtract : Int
  val _KeypadAdd : Int
  val _KeypadEnter : Int
  val _KeypadEqual : Int
  val _AppBack : Int  // Available on some keyboard/mouses. Often referred as "Browser Back"
  val _AppForward : Int
  val _Oem102 : Int  // Non-US backslash.
  val _GamepadStart : Int  // Menu | + | Options |
  val _GamepadBack : Int  // View | - | Share |
  val _GamepadFaceLeft : Int  // X | Y | Square | Toggle Menu. Hold for Windowing mode (Focus/Move/Resize windows)
  val _GamepadFaceRight : Int  // B | A | Circle | Cancel / Close / Exit
  val _GamepadFaceUp : Int  // Y | X | Triangle | Open Context Menu
  val _GamepadFaceDown : Int  // A | B | Cross | Activate / Open / Toggle. Hold for 0.60f to Activate in Text Input mode (e.g. wired to an on-screen keyboard).
  val _GamepadDpadLeft : Int  // D-pad Left | " | " | Move / Tweak / Resize Window (in Windowing mode)
  val _GamepadDpadRight : Int  // D-pad Right | " | " | Move / Tweak / Resize Window (in Windowing mode)
  val _GamepadDpadUp : Int  // D-pad Up | " | " | Move / Tweak / Resize Window (in Windowing mode)
  val _GamepadDpadDown : Int  // D-pad Down | " | " | Move / Tweak / Resize Window (in Windowing mode)
  val _GamepadL1 : Int  // L Bumper | L | L1 | Tweak Slower / Focus Previous (in Windowing mode)
  val _GamepadR1 : Int  // R Bumper | R | R1 | Tweak Faster / Focus Next (in Windowing mode)
  val _GamepadL2 : Int  // L Trigger | ZL | L2 | [Analog]
  val _GamepadR2 : Int  // R Trigger | ZR | R2 | [Analog]
  val _GamepadL3 : Int  // L Stick | L3 | L3 |
  val _GamepadR3 : Int  // R Stick | R3 | R3 |
  val _GamepadLStickLeft : Int  // | | | [Analog] Move Window (in Windowing mode)
  val _GamepadLStickRight : Int  // | | | [Analog] Move Window (in Windowing mode)
  val _GamepadLStickUp : Int  // | | | [Analog] Move Window (in Windowing mode)
  val _GamepadLStickDown : Int  // | | | [Analog] Move Window (in Windowing mode)
  val _GamepadRStickLeft : Int  // | | | [Analog]
  val _GamepadRStickRight : Int  // | | | [Analog]
  val _GamepadRStickUp : Int  // | | | [Analog]
  val _GamepadRStickDown : Int  // | | | [Analog]
  val _MouseLeft : Int  // Aliases: Mouse Buttons (auto-submitted from AddMouseButtonEvent() calls) - This is mirroring the data also written to io.MouseDown[], io.MouseWheel, in a format allowing them to be accessed via standard key API.
  val _MouseRight : Int
  val _MouseMiddle : Int
  val _MouseX1 : Int
  val _MouseX2 : Int
  val _MouseWheelX : Int
  val _MouseWheelY : Int
  val ImGuiMod_None : Int  // Keyboard Modifiers (explicitly submitted by backend via AddKeyEvent() calls) - Any functions taking a ImGuiKeyChord parameter can binary-or those with regular keys, e.g. Shortcut(ImGuiMod_Ctrl | ImGuiKey_S). - Those are written back into io.KeyCtrl, io.KeyShift, io.KeyAlt, io.KeySuper for convenience, but may be accessed via standard key API such as IsKeyPressed(), IsKeyReleased(), querying duration etc. - Code polling every key (e.g. an interface to detect a key press for input mapping) might want to ignore those and prefer using the real keys (e.g. ImGuiKey_LeftCtrl, ImGuiKey_RightCtrl instead of ImGuiMod_Ctrl). - In theory the value of keyboard modifiers should be roughly equivalent to a logical or of the equivalent left/right keys. In practice: it's complicated; mods are often provided from different sources. Keyboard layout, IME, sticky keys and backends tend to interfere and break that equivalence. The safer decision is to relay that ambiguity down to the end-user... - On macOS, we swap Cmd(Super) and Ctrl keys at the time of the io.AddKeyEvent() call.
  val ImGuiMod_Ctrl : Int  // Ctrl (non-macOS), Cmd (macOS)
  val ImGuiMod_Shift : Int  // Shift
  val ImGuiMod_Alt : Int  // Option/Menu
  val ImGuiMod_Super : Int  // Windows/Super (non-macOS), Ctrl (macOS)
}

external object ImGuiInputFlags {
  val None : Int
  val Repeat : Int  // Enable repeat. Return true on successive repeats. Default for legacy IsKeyPressed(). NOT Default for legacy IsMouseClicked(). MUST BE == 1.
  val RouteActive : Int  // Route to active item only.
  val RouteFocused : Int  // Route to windows in the focus stack (DEFAULT). Deep-most focused window takes inputs. Active item takes inputs over deep-most focused window.
  val RouteGlobal : Int  // Global route (unless a focused window or active item registered the route).
  val RouteAlways : Int  // Do not register route, poll keys directly.
  val RouteOverFocused : Int  // Option: global route: higher priority than focused route (unless active item in focused route).
  val RouteOverActive : Int  // Option: global route: higher priority than active item. Unlikely you need to use that: will interfere with every active items, e.g. Ctrl+A registered by InputText will be overridden by this. May not be fully honored as user/internal code is likely to always assume they can access keys when active.
  val RouteUnlessBgFocused : Int  // Option: global route: will not be applied if underlying background/void is focused (== no Dear ImGui windows are focused). Useful for overlay applications.
  val RouteFromRootWindow : Int  // Option: route evaluated from the point of view of root window rather than current window.
  val Tooltip : Int  // Automatically display a tooltip when hovering item [BETA] Unsure of right api (opt-in/opt-out)
}

external object ImGuiConfigFlags {
  val None : Int
  val NavEnableKeyboard : Int  // Master keyboard navigation enable flag. Enable full Tabbing + directional arrows + Space/Enter to activate. Note: some features such as basic Tabbing and CtrL+Tab are enabled by regardless of this flag (and may be disabled via other means, see #4828, #9218).
  val NavEnableGamepad : Int  // Master gamepad navigation enable flag. Backend also needs to set ImGuiBackendFlags_HasGamepad.
  val NoMouse : Int  // Instruct dear imgui to disable mouse inputs and interactions.
  val NoMouseCursorChange : Int  // Instruct backend to not alter mouse cursor shape and visibility. Use if the backend cursor changes are interfering with yours and you don't want to use SetMouseCursor() to change mouse cursor. You may want to honor requests from imgui by reading GetMouseCursor() yourself instead.
  val NoKeyboard : Int  // Instruct dear imgui to disable keyboard inputs and interactions. This is done by ignoring keyboard events and clearing existing states.
  val DockingEnable : Int  // Docking enable flags.
  val ViewportsEnable : Int  // Viewport enable flags (require both ImGuiBackendFlags_PlatformHasViewports + ImGuiBackendFlags_RendererHasViewports set by the respective backends)
  val IsSRGB : Int  // Application is SRGB-aware.
  val IsTouchScreen : Int  // Application is using a touch screen instead of a mouse.
}

external object ImGuiBackendFlags {
  val None : Int
  val HasGamepad : Int  // Backend Platform supports gamepad and currently has one connected.
  val HasMouseCursors : Int  // Backend Platform supports honoring GetMouseCursor() value to change the OS cursor shape.
  val HasSetMousePos : Int  // Backend Platform supports io.WantSetMousePos requests to reposition the OS mouse position (only used if io.ConfigNavMoveSetMousePos is set).
  val RendererHasVtxOffset : Int  // Backend Renderer supports ImDrawCmd::VtxOffset. This enables output of large meshes (64K+ vertices) while still using 16-bit indices.
  val RendererHasTextures : Int  // Backend Renderer supports ImTextureData requests to create/update/destroy textures. This enables incremental texture updates and texture reloads. See https://github.com/ocornut/imgui/blob/master/docs/BACKENDS.md for instructions on how to upgrade your custom backend.
  val RendererHasViewports : Int  // Backend Renderer supports multiple viewports.
  val PlatformHasViewports : Int  // Backend Platform supports multiple viewports.
  val HasMouseHoveredViewport : Int  // Backend Platform supports calling io.AddMouseViewportEvent() with the viewport under the mouse. IF POSSIBLE, ignore viewports with the ImGuiViewportFlags_NoInputs flag (Win32 backend, GLFW 3.30+ backend can do this, SDL backend cannot). If this cannot be done, Dear ImGui needs to use a flawed heuristic to find the viewport under.
  val HasParentViewport : Int  // Backend Platform supports honoring viewport->ParentViewport/ParentViewportId value, by applying the corresponding parent/child relationship at the Platform level. Child windows always appear in front of their parent window.
}

external object ImGuiCol {
  val Text : Int
  val TextDisabled : Int
  val WindowBg : Int  // Background of normal windows
  val ChildBg : Int  // Background of child windows
  val PopupBg : Int  // Background of popups, menus, tooltips windows
  val Border : Int
  val BorderShadow : Int
  val FrameBg : Int  // Background of checkbox, radio button, plot, slider, text input
  val FrameBgHovered : Int
  val FrameBgActive : Int
  val TitleBg : Int  // Title bar
  val TitleBgActive : Int  // Title bar when focused
  val TitleBgCollapsed : Int  // Title bar when collapsed
  val MenuBarBg : Int
  val ScrollbarBg : Int
  val ScrollbarGrab : Int
  val ScrollbarGrabHovered : Int
  val ScrollbarGrabActive : Int
  val CheckMark : Int  // Checkbox tick and RadioButton circle
  val CheckboxSelectedBg : Int  // Checkbox background when Selected, otherwise use FrameBg
  val SliderGrab : Int
  val SliderGrabActive : Int
  val Button : Int
  val ButtonHovered : Int
  val ButtonActive : Int
  val Header : Int  // Header* colors are used for CollapsingHeader, TreeNode, Selectable, MenuItem
  val HeaderHovered : Int
  val HeaderActive : Int
  val Separator : Int
  val SeparatorHovered : Int
  val SeparatorActive : Int
  val ResizeGrip : Int  // Resize grip in lower-right and lower-left corners of windows.
  val ResizeGripHovered : Int
  val ResizeGripActive : Int
  val InputTextCursor : Int  // InputText cursor/caret
  val TabHovered : Int  // Tab background, when hovered
  val Tab : Int  // Tab background, when tab-bar is focused & tab is unselected
  val TabSelected : Int  // Tab background, when tab-bar is focused & tab is selected
  val TabSelectedOverline : Int  // Tab horizontal overline, when tab-bar is focused & tab is selected
  val TabDimmed : Int  // Tab background, when tab-bar is unfocused & tab is unselected
  val TabDimmedSelected : Int  // Tab background, when tab-bar is unfocused & tab is selected
  val TabDimmedSelectedOverline : Int  // .horizontal overline, when tab-bar is unfocused & tab is selected
  val DockingPreview : Int  // Preview overlay color when about to docking something
  val DockingEmptyBg : Int  // Background color for empty node (e.g. CentralNode with no window docked into it)
  val PlotLines : Int
  val PlotLinesHovered : Int
  val PlotHistogram : Int
  val PlotHistogramHovered : Int
  val TableHeaderBg : Int  // Table header background
  val TableBorderStrong : Int  // Table outer and header borders (prefer using Alpha=1.0 here)
  val TableBorderLight : Int  // Table inner borders (prefer using Alpha=1.0 here)
  val TableRowBg : Int  // Table row background (even rows)
  val TableRowBgAlt : Int  // Table row background (odd rows)
  val TextLink : Int  // Hyperlink color
  val TextSelectedBg : Int  // Selected text inside an InputText
  val TreeLines : Int  // Tree node hierarchy outlines when using ImGuiTreeNodeFlags_DrawLines
  val DragDropTarget : Int  // Rectangle border highlighting a drop target
  val DragDropTargetBg : Int  // Rectangle background highlighting a drop target
  val UnsavedMarker : Int  // Unsaved Document marker (in window title and tabs)
  val NavCursor : Int  // Color of keyboard/gamepad navigation cursor/rectangle, when visible
  val NavWindowingHighlight : Int  // Highlight window when using Ctrl+Tab
  val NavWindowingDimBg : Int  // Darken/colorize entire screen behind the Ctrl+Tab window list, when active
  val ModalWindowDimBg : Int  // Darken/colorize entire screen behind a modal window, when one is active
  val COUNT : Int
}

external object ImGuiStyleVar {
  val Alpha : Int  // float Alpha
  val DisabledAlpha : Int  // float DisabledAlpha
  val WindowPadding : Int  // ImVec2 WindowPadding
  val WindowRounding : Int  // float WindowRounding
  val WindowBorderSize : Int  // float WindowBorderSize
  val WindowMinSize : Int  // ImVec2 WindowMinSize
  val WindowTitleAlign : Int  // ImVec2 WindowTitleAlign
  val ChildRounding : Int  // float ChildRounding
  val ChildBorderSize : Int  // float ChildBorderSize
  val PopupRounding : Int  // float PopupRounding
  val PopupBorderSize : Int  // float PopupBorderSize
  val FramePadding : Int  // ImVec2 FramePadding
  val FrameRounding : Int  // float FrameRounding
  val FrameBorderSize : Int  // float FrameBorderSize
  val ItemSpacing : Int  // ImVec2 ItemSpacing
  val ItemInnerSpacing : Int  // ImVec2 ItemInnerSpacing
  val IndentSpacing : Int  // float IndentSpacing
  val CellPadding : Int  // ImVec2 CellPadding
  val ScrollbarSize : Int  // float ScrollbarSize
  val ScrollbarRounding : Int  // float ScrollbarRounding
  val ScrollbarPadding : Int  // float ScrollbarPadding
  val GrabMinSize : Int  // float GrabMinSize
  val GrabRounding : Int  // float GrabRounding
  val ImageRounding : Int  // float ImageRounding
  val ImageBorderSize : Int  // float ImageBorderSize
  val TabRounding : Int  // float TabRounding
  val TabBorderSize : Int  // float TabBorderSize
  val TabMinWidthBase : Int  // float TabMinWidthBase
  val TabMinWidthShrink : Int  // float TabMinWidthShrink
  val TabBarBorderSize : Int  // float TabBarBorderSize
  val TabBarOverlineSize : Int  // float TabBarOverlineSize
  val TableAngledHeadersAngle : Int  // float TableAngledHeadersAngle
  val TableAngledHeadersTextAlign : Int  // ImVec2 TableAngledHeadersTextAlign
  val TreeLinesSize : Int  // float TreeLinesSize
  val TreeLinesRounding : Int  // float TreeLinesRounding
  val DragDropTargetRounding : Int  // float DragDropTargetRounding
  val ButtonTextAlign : Int  // ImVec2 ButtonTextAlign
  val SelectableTextAlign : Int  // ImVec2 SelectableTextAlign
  val SeparatorSize : Int  // float SeparatorSize
  val SeparatorTextBorderSize : Int  // float SeparatorTextBorderSize
  val SeparatorTextAlign : Int  // ImVec2 SeparatorTextAlign
  val SeparatorTextPadding : Int  // ImVec2 SeparatorTextPadding
  val DockingSeparatorSize : Int  // float DockingSeparatorSize
  val COUNT : Int
}

external object ImGuiButtonFlags {
  val None : Int
  val MouseButtonLeft : Int  // React on left mouse button (default)
  val MouseButtonRight : Int  // React on right mouse button
  val MouseButtonMiddle : Int  // React on center mouse button
  val EnableNav : Int  // InvisibleButton(): do not disable navigation/tabbing. Otherwise disabled by default.
  val AllowOverlap : Int  // Hit testing will allow subsequent widgets to overlap this one. Require previous frame HoveredId to match before being usable. Shortcut to calling SetNextItemAllowOverlap().
}

external object ImGuiColorEditFlags {
  val None : Int
  val NoAlpha : Int  // // ColorEdit, ColorPicker, ColorButton: ignore Alpha component (will only read 3 components from the input pointer).
  val NoPicker : Int  // // ColorEdit: disable picker when clicking on color square.
  val NoOptions : Int  // // ColorEdit: disable toggling options menu when right-clicking on inputs/small preview.
  val NoSmallPreview : Int  // // ColorEdit, ColorPicker: disable color square preview next to the inputs. (e.g. to show only the inputs)
  val NoInputs : Int  // // ColorEdit, ColorPicker: disable inputs sliders/text widgets (e.g. to show only the small preview color square).
  val NoTooltip : Int  // // ColorEdit, ColorPicker, ColorButton: disable tooltip when hovering the preview.
  val NoLabel : Int  // // ColorEdit, ColorPicker: disable display of inline text label (the label is still forwarded to the tooltip and picker).
  val NoSidePreview : Int  // // ColorPicker: disable bigger color preview on right side of the picker, use small color square preview instead.
  val NoDragDrop : Int  // // ColorEdit: disable drag and drop target/source. ColorButton: disable drag and drop source.
  val NoBorder : Int  // // ColorButton: disable border (which is enforced by default)
  val NoColorMarkers : Int  // // ColorEdit: disable rendering R/G/B/A color marker. May also be disabled globally by setting style.ColorMarkerSize = 0.
  val AlphaOpaque : Int  // // ColorEdit, ColorPicker, ColorButton: disable alpha in the preview,. Contrary to _NoAlpha it may still be edited when calling ColorEdit4()/ColorPicker4(). For ColorButton() this does the same as _NoAlpha.
  val AlphaNoBg : Int  // // ColorEdit, ColorPicker, ColorButton: disable rendering a checkerboard background behind transparent color.
  val AlphaPreviewHalf : Int  // // ColorEdit, ColorPicker, ColorButton: display half opaque / half transparent preview.
  val AlphaBar : Int  // // ColorEdit, ColorPicker: show vertical alpha bar/gradient in picker.
  val HDR : Int  // // (WIP) ColorEdit: Currently only disable 0.0f..1.0f limits in RGBA edition (note: you probably want to use ImGuiColorEditFlags_Float flag as well).
  val DisplayRGB : Int  // [Display] // ColorEdit: override _display_ type among RGB/HSV/Hex. ColorPicker: select any combination using one or more of RGB/HSV/Hex.
  val DisplayHSV : Int  // [Display] // "
  val DisplayHex : Int  // [Display] // "
  val Uint8 : Int  // [DataType] // ColorEdit, ColorPicker, ColorButton: _display_ values formatted as 0..255.
  val Float : Int  // [DataType] // ColorEdit, ColorPicker, ColorButton: _display_ values formatted as 0.0f..1.0f floats instead of 0..255 integers. No round-trip of value via integers.
  val PickerHueBar : Int  // [Picker] // ColorPicker: bar for Hue, rectangle for Sat/Value.
  val PickerHueWheel : Int  // [Picker] // ColorPicker: wheel for Hue, triangle for Sat/Value.
  val InputRGB : Int  // [Input] // ColorEdit, ColorPicker: input and output data in RGB format.
  val InputHSV : Int  // [Input] // ColorEdit, ColorPicker: input and output data in HSV format.
}

external object ImGuiSliderFlags {
  val None : Int
  val Logarithmic : Int  // Make the widget logarithmic (linear otherwise). Consider using ImGuiSliderFlags_NoRoundToFormat with this if using a format-string with small amount of digits.
  val NoRoundToFormat : Int  // Disable rounding underlying value to match precision of the display format string (e.g. %.3f values are rounded to those 3 digits).
  val NoInput : Int  // Disable Ctrl+Click or Enter key allowing to input text directly into the widget.
  val WrapAround : Int  // Enable wrapping around from max to min and from min to max. Only supported by DragXXX() functions for now.
  val ClampOnInput : Int  // Clamp value to min/max bounds when input manually with Ctrl+Click. By default Ctrl+Click allows going out of bounds.
  val ClampZeroRange : Int  // Clamp even if min==max==0.0f. Otherwise due to legacy reason DragXXX functions don't clamp with those values. When your clamping limits are dynamic you almost always want to use it.
  val NoSpeedTweaks : Int  // Disable keyboard modifiers altering tweak speed. Useful if you want to alter tweak speed yourself based on your own logic.
  val ColorMarkers : Int  // DragScalarN(), SliderScalarN(): Draw R/G/B/A color markers on each component.
  val AlwaysClamp : Int
}

external object ImGuiMouseButton {
  val Left : Int
  val Right : Int
  val Middle : Int
  val COUNT : Int
}

external object ImGuiMouseCursor {
  val None : Int
  val Arrow : Int
  val TextInput : Int  // When hovering over InputText, etc.
  val ResizeAll : Int  // (Unused by Dear ImGui functions)
  val ResizeNS : Int  // When hovering over a horizontal border
  val ResizeEW : Int  // When hovering over a vertical border or a column
  val ResizeNESW : Int  // When hovering over the bottom-left corner of a window
  val ResizeNWSE : Int  // When hovering over the bottom-right corner of a window
  val Hand : Int  // (Unused by Dear ImGui functions. Use for e.g. hyperlinks)
  val Wait : Int  // When waiting for something to process/load.
  val Progress : Int  // When waiting for something to process/load, but application is still interactive.
  val NotAllowed : Int  // When hovering something with disallowed interaction. Usually a crossed circle.
  val COUNT : Int
}

external object ImGuiMouseSource {
  val _Mouse : Int  // Input is coming from an actual mouse.
  val _TouchScreen : Int  // Input is coming from a touch screen (no hovering prior to initial press, less precise initial press aiming, dual-axis wheeling possible).
  val _Pen : Int  // Input is coming from a pressure/magnetic pen (often used in conjunction with high-sampling rates).
  val _COUNT : Int
}

external object ImGuiCond {
  val None : Int  // No condition (always set the variable), same as _Always
  val Always : Int  // No condition (always set the variable), same as _None
  val Once : Int  // Set the variable once per runtime session (only the first call will succeed)
  val FirstUseEver : Int  // Set the variable if the object/window has no persistently saved data (no entry in .ini file)
  val Appearing : Int  // Set the variable if the object/window is appearing after being hidden/inactive (or the first time)
}

external object ImGuiTableFlags {
  val None : Int  // Features
  val Resizable : Int  // Enable resizing columns.
  val Reorderable : Int  // Enable reordering columns in header row. (Need calling TableSetupColumn() + TableHeadersRow() to display headers, or using ImGuiTableFlags_ContextMenuInBody to access context-menu without headers).
  val Hideable : Int  // Enable hiding/disabling columns in context menu.
  val Sortable : Int  // Enable sorting. Call TableGetSortSpecs() to obtain sort specs. Also see ImGuiTableFlags_SortMulti and ImGuiTableFlags_SortTristate.
  val NoSavedSettings : Int  // Disable persisting columns order, width, visibility and sort settings in the .ini file.
  val ContextMenuInBody : Int  // Right-click on columns body/contents will also display table context menu. By default it is available in TableHeadersRow().
  val RowBg : Int  // Set each RowBg color with ImGuiCol_TableRowBg or ImGuiCol_TableRowBgAlt (equivalent of calling TableSetBgColor with ImGuiTableBgFlags_RowBg0 on each row manually)
  val BordersInnerH : Int  // Draw horizontal borders between rows.
  val BordersOuterH : Int  // Draw horizontal borders at the top and bottom.
  val BordersInnerV : Int  // Draw vertical borders between columns.
  val BordersOuterV : Int  // Draw vertical borders on the left and right sides.
  val BordersH : Int  // Draw horizontal borders.
  val BordersV : Int  // Draw vertical borders.
  val BordersInner : Int  // Draw inner borders.
  val BordersOuter : Int  // Draw outer borders.
  val Borders : Int  // Draw all borders.
  val NoBordersInBody : Int  // [ALPHA] Disable vertical borders in columns Body (borders will always appear in Headers). -> May move to style
  val NoBordersInBodyUntilResize : Int  // [ALPHA] Disable vertical borders in columns Body until hovered for resize (borders will always appear in Headers). -> May move to style
  val SizingFixedFit : Int  // Columns default to _WidthFixed or _WidthAuto (if resizable or not resizable), matching contents width.
  val SizingFixedSame : Int  // Columns default to _WidthFixed or _WidthAuto (if resizable or not resizable), matching the maximum contents width of all columns. Implicitly enable ImGuiTableFlags_NoKeepColumnsVisible.
  val SizingStretchProp : Int  // Columns default to _WidthStretch with default weights proportional to each columns contents widths.
  val SizingStretchSame : Int  // Columns default to _WidthStretch with default weights all equal, unless overridden by TableSetupColumn().
  val NoHostExtendX : Int  // Make outer width auto-fit to columns, overriding outer_size.x value. Only available when ScrollX/ScrollY are disabled and Stretch columns are not used.
  val NoHostExtendY : Int  // Make outer height stop exactly at outer_size.y (prevent auto-extending table past the limit). Only available when ScrollX/ScrollY are disabled. Data below the limit will be clipped and not visible.
  val NoKeepColumnsVisible : Int  // Disable keeping column always minimally visible when ScrollX is off and table gets too small. Not recommended if columns are resizable.
  val PreciseWidths : Int  // Disable distributing remainder width to stretched columns (width allocation on a 100-wide table with 3 columns: Without this flag: 33,33,34. With this flag: 33,33,33). With larger number of columns, resizing will appear to be less smooth.
  val NoClip : Int  // Disable clipping rectangle for every individual columns (reduce draw command count, items will be able to overflow into other columns). Generally incompatible with TableSetupScrollFreeze().
  val PadOuterX : Int  // Default if BordersOuterV is on. Enable outermost padding. Generally desirable if you have headers.
  val NoPadOuterX : Int  // Default if BordersOuterV is off. Disable outermost padding.
  val NoPadInnerX : Int  // Disable inner padding between columns (double inner padding if BordersOuterV is on, single inner padding if BordersOuterV is off).
  val ScrollX : Int  // Enable horizontal scrolling. Require 'outer_size' parameter of BeginTable() to specify the container size. Changes default sizing policy. Because this creates a child window, ScrollY is currently generally recommended when using ScrollX.
  val ScrollY : Int  // Enable vertical scrolling. Require 'outer_size' parameter of BeginTable() to specify the container size.
  val SortMulti : Int  // Hold shift when clicking headers to sort on multiple column. TableGetSortSpecs() may return specs where (SpecsCount > 1).
  val SortTristate : Int  // Allow no sorting, disable default sorting. TableGetSortSpecs() may return specs where (SpecsCount == 0).
  val HighlightHoveredColumn : Int  // Highlight column headers when hovered (may evolve into a fuller highlight)
}

external object ImGuiTableColumnFlags {
  val None : Int  // Input configuration flags
  val Disabled : Int  // Overriding/master disable flag: hide column, won't show in context menu (unlike calling TableSetColumnEnabled() which manipulates the user accessible state)
  val DefaultHide : Int  // Default as a hidden/disabled column.
  val DefaultSort : Int  // Default as a sorting column.
  val WidthStretch : Int  // Column will stretch. Preferable with horizontal scrolling disabled (default if table sizing policy is _SizingStretchSame or _SizingStretchProp).
  val WidthFixed : Int  // Column will not stretch. Preferable with horizontal scrolling enabled (default if table sizing policy is _SizingFixedFit and table is resizable).
  val NoResize : Int  // Disable manual resizing.
  val NoReorder : Int  // Disable manual reordering this column, this will also prevent other columns from crossing over this column.
  val NoHide : Int  // Disable ability to hide/disable this column.
  val NoClip : Int  // Disable clipping for this column (all NoClip columns will render in a same draw command).
  val NoSort : Int  // Disable ability to sort on this field (even if ImGuiTableFlags_Sortable is set on the table).
  val NoSortAscending : Int  // Disable ability to sort in the ascending direction.
  val NoSortDescending : Int  // Disable ability to sort in the descending direction.
  val NoHeaderLabel : Int  // TableHeadersRow() will submit an empty label for this column. Convenient for some small columns. Name will still appear in context menu or in angled headers. You may append into this cell by calling TableSetColumnIndex() right after the TableHeadersRow() call.
  val NoHeaderWidth : Int  // Disable header text width contribution to automatic column width.
  val PreferSortAscending : Int  // Make the initial sort direction Ascending when first sorting on this column (default).
  val PreferSortDescending : Int  // Make the initial sort direction Descending when first sorting on this column.
  val IndentEnable : Int  // Use current Indent value when entering cell (default for column 0).
  val IndentDisable : Int  // Ignore current Indent value when entering cell (default for columns > 0). Indentation changes _within_ the cell will still be honored.
  val AngledHeader : Int  // TableHeadersRow() will submit an angled header row for this column. Note this will add an extra row.
  val IsEnabled : Int  // Status: is enabled == not hidden by user/api (referred to as "Hide" in _DefaultHide and _NoHide) flags.
  val IsVisible : Int  // Status: is visible == is enabled AND not clipped by scrolling.
  val IsSorted : Int  // Status: is currently part of the sort specs
  val IsHovered : Int  // Status: is hovered by mouse
}

external object ImGuiTableRowFlags {
  val None : Int
  val Headers : Int  // Identify header row (set default background color + width of its contents accounted differently for auto column width)
}

external object ImGuiTableBgTarget {
  val None : Int
  val RowBg0 : Int  // Set row background color 0 (generally used for background, automatically set when ImGuiTableFlags_RowBg is used)
  val RowBg1 : Int  // Set row background color 1 (generally used for selection marking)
  val CellBg : Int  // Set cell background color (top-most color)
}

external object ImGuiListClipperFlags {
  val None : Int
  val NoSetTableRowCounters : Int  // [Internal] Disabled modifying table row counters. Avoid assumption that 1 clipper item == 1 table row.
}

external object ImGuiMultiSelectFlags {
  val None : Int
  val SingleSelect : Int  // Disable selecting more than one item. This is available to allow single-selection code to share same code/logic if desired. It essentially disables the main purpose of BeginMultiSelect() tho!
  val NoSelectAll : Int  // Disable Ctrl+A shortcut to select all.
  val NoRangeSelect : Int  // Disable Shift+selection mouse/keyboard support (useful for unordered 2D selection). With BoxSelect is also ensure contiguous SetRange requests are not combined into one. This allows not handling interpolation in SetRange requests.
  val NoAutoSelect : Int  // Disable selecting items when navigating (useful for e.g. supporting range-select in a list of checkboxes).
  val NoAutoClear : Int  // Disable clearing selection when navigating or selecting another one (generally used with ImGuiMultiSelectFlags_NoAutoSelect. useful for e.g. supporting range-select in a list of checkboxes).
  val NoAutoClearOnReselect : Int  // Disable clearing selection when clicking/selecting an already selected item.
  val BoxSelect1d : Int  // Enable box-selection with same width and same x pos items (e.g. full row Selectable()). Box-selection works better with little bit of spacing between items hit-box in order to be able to aim at empty space.
  val BoxSelect2d : Int  // Enable box-selection with varying width or varying x pos items support (e.g. different width labels, or 2D layout/grid). This is slower: alters clipping logic so that e.g. horizontal movements will update selection of normally clipped items.
  val BoxSelectNoScroll : Int  // Disable scrolling when box-selecting and moving mouse near edges of scope.
  val ClearOnEscape : Int  // Clear selection when pressing Escape while scope is focused.
  val ClearOnClickVoid : Int  // Clear selection when clicking on empty location within scope.
  val ScopeWindow : Int  // Scope for _BoxSelect and _ClearOnClickVoid is whole window (Default). Use if BeginMultiSelect() covers a whole window or used a single time in same window.
  val ScopeRect : Int  // Scope for _BoxSelect and _ClearOnClickVoid is rectangle encompassing BeginMultiSelect()/EndMultiSelect(). Use if BeginMultiSelect() is called multiple times in same window.
  val SelectOnAuto : Int  // Apply selection on mouse down when clicking on unselected item, on mouse up when clicking on selected item. (Default)
  val SelectOnClickAlways : Int  // Apply selection on mouse down when clicking on any items. Prevents Drag and Drop from being used on multiple-selection, but allows e.g. BoxSelect to always reselect even when clicking inside an existing selection. (Excel style behavior)
  val SelectOnClickRelease : Int  // Apply selection on mouse release when clicking an unselected item. Allow dragging an unselected item without altering selection.
  val NavWrapX : Int  // [Temporary] Enable navigation wrapping on X axis. Provided as a convenience because we don't have a design for the general Nav API for this yet. When the more general feature be public we may obsolete this flag in favor of new one.
  val NoSelectOnRightClick : Int  // Disable default right-click processing, which selects item on mouse down, and is designed for context-menus.
}

external object ImGuiSelectionRequestType {
  val _None : Int
  val _SetAll : Int  // Request app to clear selection (if Selected==false) or select all items (if Selected==true). We cannot set RangeFirstItem/RangeLastItem as its contents is entirely up to user (not necessarily an index)
  val _SetRange : Int  // Request app to select/unselect [RangeFirstItem..RangeLastItem] items (inclusive) based on value of Selected. Only EndMultiSelect() request this, app code can read after BeginMultiSelect() and it will always be false.
}

external object ImDrawFlags {
  val None : Int
  val RoundCornersTopLeft : Int  // AddRect(), AddRectFilled(), PathRect(): enable rounding top-left corner only (when rounding > 0.0f, we default to all corners). Was 0x01.
  val RoundCornersTopRight : Int  // AddRect(), AddRectFilled(), PathRect(): enable rounding top-right corner only (when rounding > 0.0f, we default to all corners). Was 0x02.
  val RoundCornersBottomLeft : Int  // AddRect(), AddRectFilled(), PathRect(): enable rounding bottom-left corner only (when rounding > 0.0f, we default to all corners). Was 0x04.
  val RoundCornersBottomRight : Int  // AddRect(), AddRectFilled(), PathRect(): enable rounding bottom-right corner only (when rounding > 0.0f, we default to all corners). Wax 0x08.
  val RoundCornersNone : Int  // AddRect(), AddRectFilled(), PathRect(): disable rounding on all corners (when rounding > 0.0f). This is NOT zero, NOT an implicit flag!
  val Closed : Int  // PathStroke(), AddPolyline(): specify that shape should be closed (Important: this is always == 1 for legacy reason)
  val RoundCornersTop : Int
  val RoundCornersBottom : Int
  val RoundCornersLeft : Int
  val RoundCornersRight : Int
  val RoundCornersAll : Int
}

external object ImDrawListFlags {
  val None : Int
  val AntiAliasedLines : Int  // Enable anti-aliased lines/borders (*2 the number of triangles for 1.0f wide line or lines thin enough to be drawn using textures, otherwise *3 the number of triangles)
  val AntiAliasedLinesUseTex : Int  // Enable anti-aliased lines/borders using textures when possible. Require backend to render with bilinear filtering (NOT point/nearest filtering).
  val AntiAliasedFill : Int  // Enable anti-aliased edge around filled shapes (rounded rectangles, circles).
  val AllowVtxOffset : Int  // Can emit 'VtxOffset > 0' to allow large meshes. Set when 'ImGuiBackendFlags_RendererHasVtxOffset' is enabled.
}

external object ImTextureFormat {
  val _RGBA32 : Int  // 4 components per pixel, each is unsigned 8-bit. Total size = TexWidth * TexHeight * 4
  val _Alpha8 : Int  // 1 component per pixel, each is unsigned 8-bit. Total size = TexWidth * TexHeight
}

external object ImTextureStatus {
  val _OK : Int
  val _Destroyed : Int  // Backend destroyed the texture.
  val _WantCreate : Int  // Requesting backend to create the texture. Set status OK when done.
  val _WantUpdates : Int  // Requesting backend to update specific blocks of pixels (write to texture portions which have never been used before). Set status OK when done.
  val _WantDestroy : Int  // Requesting backend to destroy the texture. Set status to Destroyed when done.
}

external object ImFontAtlasFlags {
  val None : Int
  val NoPowerOfTwoHeight : Int  // Don't round the height to next power of two
  val NoMouseCursors : Int  // Don't build software mouse cursors into the atlas (save a little texture memory)
  val NoBakedLines : Int  // Don't build thick line textures into the atlas (save a little texture memory, allow support for point/nearest filtering). The AntiAliasedLinesUseTex features uses them, otherwise they will be rendered using polygons (more expensive for CPU/GPU).
}

external object ImFontFlags {
  val None : Int
  val NoLoadError : Int  // Disable throwing an error/assert when calling AddFontXXX() with missing file/data. Calling code is expected to check AddFontXXX() return value.
  val NoLoadGlyphs : Int  // [Internal] Disable loading new glyphs.
  val LockBakedSizes : Int  // [Internal] Disable loading new baked sizes, disable garbage collecting current ones. e.g. if you want to lock a font to a single size. Important: if you use this to preload given sizes, consider the possibility of multiple font density used on Retina display.
  val ImplicitRefSize : Int  // [Internal] Reference size was not set explicitly.
}

external object ImGuiViewportFlags {
  val None : Int
  val IsPlatformWindow : Int  // Represent a Platform Window
  val IsPlatformMonitor : Int  // Represent a Platform Monitor (unused yet)
  val OwnedByApp : Int  // Platform Window: Is created/managed by the user application? (rather than our backend)
  val NoDecoration : Int  // Platform Window: Disable platform decorations: title bar, borders, etc. (generally set all windows, but if ImGuiConfigFlags_ViewportsDecoration is set we only set this on popups/tooltips)
  val NoTaskBarIcon : Int  // Platform Window: Disable platform task bar icon (generally set on popups/tooltips, or all windows if ImGuiConfigFlags_ViewportsNoTaskBarIcon is set)
  val NoFocusOnAppearing : Int  // Platform Window: Don't take focus when created.
  val NoFocusOnClick : Int  // Platform Window: Don't take focus when clicked on.
  val NoInputs : Int  // Platform Window: Make mouse pass through so we can drag this window while peaking behind it.
  val NoRendererClear : Int  // Platform Window: Renderer doesn't need to clear the framebuffer ahead (because we will fill it entirely).
  val NoAutoMerge : Int  // Platform Window: Avoid merging this window into another host window. This can only be set via ImGuiWindowClass viewport flags override (because we need to now ahead if we are going to create a viewport in the first place!).
  val TopMost : Int  // Platform Window: Display on top (for tooltips only).
  val CanHostOtherWindows : Int  // Viewport can host multiple imgui windows (secondary viewports are associated to a single window). // FIXME: In practice there's still probably code making the assumption that this is always and only on the MainViewport. Will fix once we add support for "no main viewport".
  val IsMinimized : Int  // Platform Window: Window is minimized, can skip render. When minimized we tend to avoid using the viewport pos/size for clipping window or testing if they are contained in the viewport.
  val IsFocused : Int  // Platform Window: Window is focused (last call to Platform_GetWindowFocus() returned true)
}

external object FreeTypeLoaderFlags {
  val NoHinting : Int
  val NoAutoHint : Int
  val ForceAutoHint : Int
  val LightHinting : Int
  val MonoHinting : Int
  val Bold : Int
  val Oblique : Int
  val Monochrome : Int
  val LoadColor : Int
  val Bitmap : Int
}
