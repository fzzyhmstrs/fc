package me.fzzyhmstrs.fzzy_core.config_util.gui

interface ConfigGuiEntry{
    fun widget(theme: Theme): ThemedWidget
    fun widgetHeight(): Int
    fun widgetWidth(): Int
    fun locked(): Boolean
}
