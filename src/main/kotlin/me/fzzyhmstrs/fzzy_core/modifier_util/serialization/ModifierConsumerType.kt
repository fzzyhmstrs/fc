package me.fzzyhmstrs.fzzy_core.modifier_util.serialization

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.base_consumers.*
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface ModifierConsumerType<T: ModifierConsumer> {
    fun codec():Codec<T>

    companion object{
        val REGISTRY = FzzyPort.simpleRegistry<ModifierConsumerType<*>>(Identifier(FC.MOD_ID,"modifier_consumer_type"))
        val CODEC: Codec<ModifierConsumer> = REGISTRY.codec.dispatch({ p: ModifierConsumer -> p.getType()},{ t -> t.codec()})
        val LIST_CODEC: Codec<List<ModifierConsumer>> = Codec.either(CODEC, CODEC.listOf()).xmap(
            {e -> e.map({l -> listOf(l)},{r -> r})},
            {l -> if (l.size == 1) Either.left(l[0]) else Either.right(l)}
        )

        fun <T: ModifierConsumer> register(type: ModifierConsumerType<T>, identifier: Identifier): ModifierConsumerType<T> {
            return Registry.register(REGISTRY,identifier,type)
        }
    }

    object Types{
        fun init(){}



        val RANDOM_CHANCE = register(RandomChanceModifierConsumer, Identifier(FC.MOD_ID,"chance"))
        val STATUS_EFFECT = register(StatusEffectModifierConsumer, Identifier(FC.MOD_ID,"status"))
        val STATUS_EFFECT_STACKING = register(StackingStatusEffectModifierConsumer, Identifier(FC.MOD_ID,"status_stacking"))
        val LIST = register(ListModifierConsumer, Identifier(FC.MOD_ID,"list"))
        val PREDICATE = register(PredicateModifierConsumer, Identifier(FC.MOD_ID,"predicate"))

    }

}