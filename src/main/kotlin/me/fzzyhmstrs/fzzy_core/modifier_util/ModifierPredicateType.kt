package me.fzzyhmstrs.fzzy_core.modifier_util

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.base_predicates.*
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface ModifierPredicateType<T:ModifierPredicate> {

    fun codec(): Codec<T>

    companion object{
        val REGISTRY = FzzyPort.simpleRegistry<ModifierPredicateType<*>>(Identifier(FC.MOD_ID,"modifier_predicate_type"))
        val CODEC: Codec<ModifierPredicate> = REGISTRY.codec.dispatch({p: ModifierPredicate -> p.getType()},{t -> t.codec()})

        fun <T: ModifierPredicate> register(type: ModifierPredicateType<T>, identifier: Identifier): ModifierPredicateType<T>{
            return Registry.register(REGISTRY,identifier,type)
        }

        /*fun test(){
            Types.init()
            val obj = JsonObject()

            obj.add("perLvlITest1", PerLvlI.CODEC.encodeStart(JsonOps.INSTANCE,PerLvlI(1,1,1)).getOrThrow(true,{}))
            obj.add("perLvlITest2", PerLvlI.CODEC.encodeStart(JsonOps.INSTANCE,PerLvlI(1)).getOrThrow(true,{}))
            obj.add("predicate",CODEC.encodeStart(JsonOps.INSTANCE,ItemTagModifierPredicate(ConventionalItemTags.SHIELDS)).getOrThrow(true,{}))
            println(obj)

            println(Test.CODEC.decode(JsonOps.INSTANCE,obj))

        }*/

        /*private class Test(val pli1: PerLvlI, val pli2: PerLvlI, val predicate: ModifierPredicate){
            companion object{
                val CODEC = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<Test> ->
                    instance.group(
                        PerLvlI.CODEC.fieldOf("perLvlITest1").forGetter { test -> test.pli1 },
                        PerLvlI.CODEC.fieldOf("perLvlITest2").forGetter { test -> test.pli2 },
                        ModifierPredicateType.CODEC.fieldOf("predicate").forGetter { test -> test.predicate }
                    ).apply(instance){p1,p2,p -> Test(p1,p2,p)}
                }
            }

            override fun toString(): String {
                return "Test[$pli1 | $pli2 | $predicate]"
            }

        }*/

    }

    object Types{

        fun init(){}

        val ENCHANTMENT_TAG = register(EnchantmentTagModifierPredicate.Type, Identifier(FC.MOD_ID,"tag_enchant"))
        val ITEM_TAG = register(ItemTagModifierPredicate.Type, Identifier(FC.MOD_ID,"tag_item"))
        val ENTITY_TYPE_TAG = register(EntityTagModifierPredicate.Type, Identifier(FC.MOD_ID,"tag_entity"))
        val INVERT = register(InvertModifierPredicate.Type, Identifier(FC.MOD_ID,"invert"))
        val AND = register(AndModifierPredicate.Type, Identifier(FC.MOD_ID,"and"))
        val OR = register(OrModifierPredicate.Type, Identifier(FC.MOD_ID,"or"))
    }

}