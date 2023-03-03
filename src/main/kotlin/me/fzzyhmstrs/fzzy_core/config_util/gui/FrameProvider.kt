package me.fzzyhmstrs.fzzy_core.config_util.gui

interface FrameProvider {
    fun hovered(): Frame
    fun unhovered():Frame
    fun clicked():Frame
    fun unclicked():Frame
    fun locked(): Frame
    fun toggle(toggle: Boolean)
}