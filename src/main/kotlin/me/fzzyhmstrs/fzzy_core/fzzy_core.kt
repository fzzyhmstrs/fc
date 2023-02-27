package me.fzzyhmstrs.fzzy_core

import com.llamalad7.mixinextras.MixinExtrasBootstrap
import me.fzzyhmstrs.fzzy_core.config.FcConfig
import me.fzzyhmstrs.fzzy_core.config.FcTestConfig
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import me.fzzyhmstrs.fzzy_core.registry.ItemModelRegistry
import me.fzzyhmstrs.fzzy_core.registry.LootRegistry
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
import net.minecraft.util.Identifier
import kotlin.random.Random

import org.slf4j.Logger
import org.slf4j.LoggerFactory


object FC: ModInitializer {
    const val MOD_ID = "fzzy_core"
    val LOGGER = LoggerFactory.getLogger("emi_loot")
    val fcRandom = Random(System.currentTimeMillis())
    val fallbackId = Identifier("vanishing_curse")

    override fun onInitialize() {
        FcConfig.initConfig()
        FcTestConfig.initConfig()
        LootRegistry.registerAll()
        EventRegistry.registerAll()
        ModifierRegistry.registerAll()
    }
}

object FCC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        ItemModelRegistry.registerAll()
        EventRegistry.registerClient()
    }
}

object FCPreLaunch: PreLaunchEntrypoint{

    override fun onPreLaunch() {
        MixinExtrasBootstrap.init()
    }

}
