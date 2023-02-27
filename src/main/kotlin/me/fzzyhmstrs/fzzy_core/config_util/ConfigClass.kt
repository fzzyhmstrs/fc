package me.fzzyhmstrs.fzzy_core.config_util

open class ConfigClass(
    headerText: List<String> = listOf(),
    decorator: Decorator = Decorator.DEFAULT)
    :
    ReadMeBuilder("default", headerText = headerText, decorator = decorator),
    ServerClientSynced