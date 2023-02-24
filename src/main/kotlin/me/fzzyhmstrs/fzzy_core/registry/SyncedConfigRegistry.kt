package me.fzzyhmstrs.fzzy_core.registry

import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper
import me.fzzyhmstrs.fzzy_core.config_util.ReadMeBuilder
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfig
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.util.Identifier

/**
 * Register a [SyncedConfigHelper.SyncedConfig] here. Synced configs will automatically synchronize config data between the clients and server. See KDoc for the SyncedConfig for instructions on setting one up.
 */

object SyncedConfigRegistry {

    private val SYNC_CONFIG_PACKET = Identifier(FC.MOD_ID,"sync_config_packet")
    private val configs : MutableMap<String,SyncedConfigHelper.SyncedConfig> = mutableMapOf()
    private val newConfigs : MutableMap<String, SyncedConfig> = mutableMapOf()

    internal fun registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_CONFIG_PACKET) { _, _, buf, _ ->
            val id = buf.readString()
            if (configs.containsKey(id)){
                configs[id]?.readFromServer(buf)
            }
            if (newConfigs.containsKey(id)){
                newConfigs[id]?.readFromServer(buf)
            }
        }
    }

    internal fun registerServer() {
        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            val player = handler.player
            configs.forEach {
                val buf = PacketByteBufs.create()
                buf.writeString(it.key)
                it.value.writeToClient(buf)
                ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf)
            }
            newConfigs.forEach {
                val buf = PacketByteBufs.create()
                buf.writeString(it.key)
                it.value.writeToClient(buf)
                ServerPlayNetworking.send(player, SYNC_CONFIG_PACKET, buf)
            }
        }
    }

    /**
     * register your config with this.
     *
     * Recommended implementation is to call this method within the overridden initConfig() method of [SyncedConfigHelper.SyncedConfig]
     *
     * initConfig() must then be called in your ModIntializer in order to complete the registration.
     *
     * [id] is a unique identifier for your config. The Mod ID is a typical choice.
     */
    @Deprecated("Scheduled for Removal; SyncedConfig used here is to be replaced with validated config system")
    fun registerConfig(id: String,config: SyncedConfigHelper.SyncedConfig){
        configs[id] = config
    }

    fun registerConfig(id: String, config: SyncedConfig){
        newConfigs[id] = config
        if (config is ReadMeBuilder){
            config.build()
            config.writeReadMe()
        }
    }
}