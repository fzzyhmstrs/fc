package me.fzzyhmstrs.fzzy_core.config_util

import net.minecraft.network.PacketByteBuf

interface SyncedConfig: ClientServerSynced{
    fun initConfig()
}
