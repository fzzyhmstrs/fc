package me.fzzyhmstrs.fzzy_core.config_util.gui

abstract class Animatable{
    fun hoveredFrame(): Frame
    fun unHoveredFrame(): Frame
    fun clickedFrame(): Frame
    fun releasedFrame(): Frame
    fun init()
}
