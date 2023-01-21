package me.fzzyhmstrs.fzzy_core.trinket_util

import net.fabricmc.loader.api.FabricLoader

object TrinketChecker {

    val trinketsLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("trinkets")
    }

}