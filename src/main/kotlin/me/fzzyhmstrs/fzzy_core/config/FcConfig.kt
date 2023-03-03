package me.fzzyhmstrs.fzzy_core.config

import com.google.gson.*
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.config_util.ConfigSection
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1.deserializeConfig
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1.readOrCreateAndValidate
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1.serializeConfig
import me.fzzyhmstrs.fzzy_core.config_util.validated_field.*
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Predicate

object FcConfig: SyncedConfigHelper.SyncedConfig {


    var flavors: Flavors

    init{
        flavors = SyncedConfigHelper.readOrCreate("flavors_v0.json") { Flavors() }
        ReadmeText.writeReadMe("README.txt", FC.MOD_ID)
    }

    override fun readFromServer(buf: PacketByteBuf) {
        flavors = gson.fromJson(buf.readString(),Flavors::class.java)
    }

    override fun writeToClient(buf: PacketByteBuf) {
        val gson = GsonBuilder().create()
        buf.writeString(gson.toJson(flavors))
    }

    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(FC.MOD_ID,this)
    }

    class Flavors{
        var showFlavorDesc: Boolean = false
        var showFlavorDescOnAdvanced: Boolean = true
    }
}
