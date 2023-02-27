package me.fzzyhmstrs.fzzy_core.config

import com.google.common.reflect.TypeToken
import me.fzzyhmstrs.fzzy_core.config_util.*
import me.fzzyhmstrs.fzzy_core.config_util.validated_field.*
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object FcTestConfig:
    SyncedConfigWithReadMe(
        "fc_test_config",
        "test_README.txt",
        headerText = listOf(
            "This is a test README",
            "I'm trying to do lots of cool automagical stuff whee",
            "",
            "So cool",
            ""
        )) {

    var test: Test = SyncedConfigHelperV1.readOrCreateAndValidate("new_test_v0.json") { Test() }

    class Test: ConfigClass(){
        var testSection_1 = object: ConfigSection(listOf(
            "",
            "_____________________",
            "Test Section 1 Header",
            ""
        )){
            @ReadMeText("Testing creating a custom readme entry for a field")
            var test_Int_1  = ValidatedInt(0,5,-5)
            var test_Int_2 = ValidatedInt(1000,10000000)
            @ReadMeText(header = [" >> Testing a custom header-only annotation"])
            var innerSection_1 = object: ConfigSection(decorator = Decorator.INNER){
                var test_Float_1 = ValidatedFloat(1f,6f)
                var test_Double = ValidatedDouble(0.0,1.0)
            }
        }

        @ReadMeText("Testing overriding a sections readme", ["","Testing an annotated header"])
        var testSection_2 = object: ConfigSection(listOf(
            "",
            "_____________________",
            "Test Section 2 Header",
            ""
        )){
            var test_Enum = ValidatedEnum(Testy.BASIC,Testy::class.java)
            var test_Bool = ValidatedBoolean(true)
        }

        var testSection_3 = object: ConfigSection(listOf(
            "",
            "_____________________",
            "Test Section 3 Header",
            ""
        )){
            var test_Id = ValidatedIdentifier(Identifier("redstone"), {id -> Registries.ITEM.containsId(id)}, "ID needs to be in the item registry.")
            var test_List = ValidatedList(
                listOf(1, 3, 5, 7),
                Int::class.java,
                {i -> i > 0},
                "Values need to be greater than 0"
            )
        }

        @ReadMeText("Some more Readme testing for this map of strings and booleans")
        var testMap = ValidatedStringKeyMap(
            mapOf(
                "minecraft:diamond" to true,
                "minecraft:stick" to true,
                "minecraft:redstone" to false
            ),
            Boolean::class.java,
            {id,_ -> val idChk = Identifier.tryParse(id); if(idChk == null){false} else {Registries.ITEM.containsId(idChk)}  },
            "Map key needs to be a valid item identifier; map entry needs to be a boolean ('true' or 'false')"
        )

    }

    enum class Testy{
        OPTION_A,
        OPTION_B,
        OPTION_C,
        BASIC
    }
}