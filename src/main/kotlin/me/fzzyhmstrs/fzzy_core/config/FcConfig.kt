package me.fzzyhmstrs.fzzy_core.config

import com.google.gson.*
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
        test = deserializeConfig(test, JsonParser.parseString(buf.readString).get())
    }

    override fun writeToClient(buf: PacketByteBuf) {
        val gson = GsonBuilder().create()
        buf.writeString(gson.toJson(flavors))
        buf.writeString(SyncedConfigHelper.serializeConfig(test))
    }

    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(FC.MOD_ID,this)
    }

    class Test{
        var testInt: SyncedConfigHelper.ValidatedInt = SyncedConfigHelper.ValidatedInt(1,10)
        var testFloat: SyncedConfigHelper.ValidatedFloat = SyncedConfigHelper.ValidatedFloat(2.5f,150f,1f)
        var testEnum: SyncedConfigHelper.ValidatedEnum = SyncedConfigHelper.ValidatedEnum(TestEnum.DEFAULT,TestEnum::Class.java)
        var testId: SyncedConfigHelper.ValidatedIdentifier = SyncedConfigHelper.ValidatedIdentifier(Identifier("minecraft","diamond"),{id -> Registries.ITEM.containsId(id)},"Needs to be a valid registered Item identifier, use advanced tooltips for help with ids in-game.")
        var testList: SyncedConfigHelper.ValidatedList = SyncedConfigHelper.ValidatedList(listOf("minecraft:diamond", "minecraft:obsidian", "minecraft:emerald"), object: TypeToken<List<String>>{},{str -> listIdValidator(str,{id -> Registries.ITEM.containsId(id)})}, "List entries need to be valid identifiers that are in the Item registry.")
        
        private fun listIdValidator(str: String, predicate: Predicate<Identifier>): Boolean{
            val id = Identifier.tryParse(str)
            if (id == null) return false
            return predicate.test(id)
        }
    }

    class Flavors{
        var showFlavorDesc: Boolean = false
        var showFlavorDescOnAdvanced: Boolean = true
    }
    
    enum TestEnum{
        OPTION_1,
        OPTION_2,
        DEFAULT
    }

}
