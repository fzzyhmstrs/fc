package me.fzzyhmstrs.fzzy_core.config_util

import me.fzzyhmstrs.fzzy_core.FC

abstract class SyncedConfigWithReadMe(file: String, base: String = FC.MOD_ID, headerText: List<String> = listOf()): ReadMeBuilder(file,base,headerText),
    SyncedConfig