package me.fzzyhmstrs.fzzy_core.trinket_util

import net.fabricmc.loader.api.FabricLoader

object TrinketChecker {

    val trinketsLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("trinkets")
    }

    fun getTrinketStacks(user: LivingEntity): List<ItemStack>{
        if (trinketsLoaded)
            return TrinketUtil.getTrinketStacks(user)
        return listOf()
    }
}
