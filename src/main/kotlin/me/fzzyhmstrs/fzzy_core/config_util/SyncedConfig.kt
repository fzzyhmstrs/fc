package me.fzzyhmstrs.fzzy_core.config_util

import net.minecraft.network.PacketByteBuf

interface SyncedConfig{
    fun readFromServer(buf: PacketByteBuf)
    fun writeToClient(buf: PacketByteBuf)
    fun initConfig()
}