package me.fzzyhmstrs.fzzy_core.config_util

open class ConfigClass(
    headerText: List<String> = listOf(),
    decorator: LineDecorator = LineDecorator.DEFAULT)
    :
    ReadMeBuilder("default", headerText = headerText, decorator = decorator),
    ServerClientSynced
{
    constructor(configLabel: String): this(listOf(configLabel))
}