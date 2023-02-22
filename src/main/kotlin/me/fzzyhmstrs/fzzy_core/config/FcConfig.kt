package me.fzzyhmstrs.fzzy_core.config

import com.google.gson.GsonBuilder
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateAndValidate
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object FcConfig: SyncedConfigHelper.SyncedConfig {


    var flavors: Flavors
    var test: Test

    init{
        flavors = SyncedConfigHelper.readOrCreate("flavors_v0.json") { Flavors() }
        test = readOrCreateAndValidate("test_v0.json"){ Test() }
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

    class Test{
        var testInt: SyncedConfigHelper.ValidatedInt = SyncedConfigHelper.ValidatedInt(1,10)
        var testFloat: SyncedConfigHelper.ValidatedFloat = SyncedConfigHelper.ValidatedFloat(2.5f,150f,1f)
        val testId: SyncedConfigHelper.ValidatedIdentifier = SyncedConfigHelper.ValidatedIdentifier(Identifier("minecraft","diamond"),{id -> Registries.ITEM.containsId(id)},"Needs to be a valid item identifier, use advanced tooltips for help with ids in-game.")
    }

    class Flavors{
        var showFlavorDesc: Boolean = false
        var showFlavorDescOnAdvanced: Boolean = true
    }

}