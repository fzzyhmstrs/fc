package me.fzzyhmstrs.fzzy_core.config_util

import net.minecraft.network.PacketByteBuf

abstract class SyncedConfig: ClientServerSynced{
    abstract fun initConfig()
}
