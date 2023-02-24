package me.fzzyhmstrs.fzzy_core.config

import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.config_util.ClientServerSynced
import me.fzzyhmstrs.fzzy_core.config_util.Section
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigHelperV1
import me.fzzyhmstrs.fzzy_core.config_util.SyncedConfigWithReadMe
import me.fzzyhmstrs.fzzy_core.config_util.validated_field.*
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.function.Predicate

object FcTestConfig:
    SyncedConfigWithReadMe("test_README.txt",
        headerText = listOf(
            "This is a test README",
            "I'm trying to do lots of cool automagical stuff whee",
            "",
            "",
            "So cool",
            ""
        )) {
    override fun initConfig() {
        SyncedConfigRegistry.registerConfig("test_fzzy_config",this)
    }

    var test: Test = SyncedConfigHelperV1.readOrCreateAndValidate("new_test_v0") { Test() }

    class Test: ClientServerSynced{
        var testSection_1 = object: Section(){
            var test_Int_1  = ValidatedInt(0,5,-5)
            var test_Int_2 = ValidatedInt(1000,10000000)
            var innerSection_1 = object: Section(){
                var test_Float_1 = ValidatedFloat(1f,6f)
                var test_Double = ValidatedDouble(0.0,1.0)
            }
        }

        var testSection_2 = object: Section(){
            var test_Enum = ValidatedEnum(Testy.BASIC,Testy::class.java)
            var test_Bool = ValidatedBoolean(true)
        }

        var testSection_3 = object: Section(){
            var test_Id = ValidatedIdentifier(Identifier("redstone"), {id -> Registries.ITEM.containsId(id)}, "ID needs to be in the item registry.")
            var test_List = ValidatedList(
                listOf(1, 3, 5, 7),
                Int::class.java,
                {i -> i > 0},
                "Values need to be greater than 0"
            )
        }

    }

    enum class Testy{
        OPTION_A,
        OPTION_B,
        OPTION_C,
        BASIC
    }
}