package me.fzzyhmstrs.fzzy_core.config_util

import net.minecraft.network.PacketByteBuf

interface ClientServerSynced{
    fun readFromServer(buf: PacketByteBuf)
    fun writeToClient(buf: PacketByteBuf)
}
