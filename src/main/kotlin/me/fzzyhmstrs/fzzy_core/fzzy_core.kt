package me.fzzyhmstrs.fzzy_core

import me.fzzyhmstrs.fzzy_core.config.FcConfig
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import me.fzzyhmstrs.fzzy_core.registry.ItemModelRegistry
import me.fzzyhmstrs.fzzy_core.registry.LootRegistry
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.random.Random


object FC: ModInitializer {
    const val MOD_ID = "fzzy_core"
    val fcRandom = Random(System.currentTimeMillis())
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        FcConfig.initConfig()
        LootRegistry.registerAll()
        EventRegistry.registerAll()
        ModifierRegistry.registerAll()
    }
}

@Suppress("unused")
object FCC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        ItemModelRegistry.registerAll()
        EventRegistry.registerClient()
    }
}