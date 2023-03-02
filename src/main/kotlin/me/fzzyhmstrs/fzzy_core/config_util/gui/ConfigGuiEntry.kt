package me.fzzyhmstrs.fzzy_core.config_util.gui

interface ConfigGuiEntry{
    fun widgets(theme: Theme): List<ThemedWidget>
}
