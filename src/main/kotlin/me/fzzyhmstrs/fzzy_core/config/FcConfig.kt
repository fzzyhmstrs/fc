package me.fzzyhmstrs.fzzy_core.config

import com.google.common.reflect.TypeToken
import com.google.gson.*
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.config_util.Section
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
    var test: Test = Test()

    init{
        flavors = SyncedConfigHelper.readOrCreate("flavors_v0.json") { Flavors() }
        setTestAndPrint(readOrCreateAndValidate("test_v0.json"){ Test() })
        ReadmeText.writeReadMe("README.txt", FC.MOD_ID)
    }

    private fun setTestAndPrint(test: Test){
        this.test = test
        println("Config Currently Is:")
        println("unval-int: " + FcConfig.test.testUnvalidatedInt)
        println("unval-string: " + FcConfig.test.testUnvalidatedString)
        println("int:" + FcConfig.test.testInt.get())
        println("float: " + FcConfig.test.testFloat.get())
        println("enum: " + FcConfig.test.testEnum.get())
        println("id: " + FcConfig.test.testId.get())
        println("list: " + FcConfig.test.testList.get())
        println("Section: " + FcConfig.test.testSection)
    }

    override fun readFromServer(buf: PacketByteBuf) {
        flavors = gson.fromJson(buf.readString(),Flavors::class.java)
        setTestAndPrint(deserializeConfig(test, JsonParser.parseString(buf.readString())).get())
    }

    override fun writeToClient(buf: PacketByteBuf) {
        val gson = GsonBuilder().create()
        buf.writeString(gson.toJson(flavors))
        buf.writeString(serializeConfig(test))
    }

    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(FC.MOD_ID,this)
    }

    class Test{
        var testUnvalidatedInt: Int = 10
        var testUnvalidatedString: String = "minecraft:iron_ingot"
        var testInt: ValidatedInt = ValidatedInt(1,10)
        var testFloat: ValidatedFloat = ValidatedFloat(2.5f,150f,1f)
        var testEnum: ValidatedEnum<TestEnum> = ValidatedEnum(TestEnum.DEFAULT,TestEnum::class.java)
        var testId: ValidatedIdentifier = ValidatedIdentifier(Identifier("minecraft","diamond"),{ id -> Registries.ITEM.containsId(id)},"Needs to be a valid registered Item identifier, use advanced tooltips for help with ids in-game.")
        var testList: ValidatedList<String> = ValidatedList(listOf("minecraft:diamond", "minecraft:obsidian", "minecraft:emerald"), String::class.java,{ str -> listIdValidator(str) { id -> Registries.ITEM.containsId(id) } }, "List entries need to be valid identifiers that are in the Item registry.")
        var testSection: Section = object: Section(){
            var testSectionInt: ValidatedInt = ValidatedInt(0,10,-10)
            var testSectionBoolean: ValidatedBoolean = ValidatedBoolean(true)
        }
        
        private fun listIdValidator(str: String, predicate: Predicate<Identifier>): Boolean{
            val id = Identifier.tryParse(str) ?: return false
            return predicate.test(id)
        }
    }

    class Flavors{
        var showFlavorDesc: Boolean = false
        var showFlavorDescOnAdvanced: Boolean = true
    }
    
    enum class TestEnum{
        OPTION_1,
        OPTION_2,
        DEFAULT
    }

}
