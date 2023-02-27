package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry

abstract class SyncedConfigWithReadMe(
    private val configName: String,
    file: String,
    base: String = FC.MOD_ID,
    headerText: List<String> = listOf(),
    decorator: Decorator = Decorator.DEFAULT)
    :
    ReadMeBuilder(file,base,headerText, decorator),
    SyncedConfig
{
    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(configName,this)
    }
}