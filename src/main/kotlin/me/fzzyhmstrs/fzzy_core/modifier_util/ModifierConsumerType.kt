package me.fzzyhmstrs.fzzy_core.modifier_util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.base_consumers.*
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

interface ModifierConsumerType<T: ModifierConsumer> {
    fun codec():Codec<T>

    companion object{
        val REGISTRY = FzzyPort.simpleRegistry<ModifierConsumerType<*>>(Identifier(FC.MOD_ID,"modifier_consumer_type"))
        val CODEC: Codec<ModifierConsumer> = REGISTRY.codec.dispatch({ p: ModifierConsumer -> p.getType()},{ t -> t.codec()})

        fun <T: ModifierConsumer> register(type: ModifierConsumerType<T>, identifier: Identifier): ModifierConsumerType<T>{
            return Registry.register(REGISTRY,identifier,type)
        }
    }

    object Types{
        fun init(){}

        val statusEffectInstanceCodec: Codec<StatusEffectInstance> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<StatusEffectInstance> ->
            instance.group(
                FzzyPort.STATUS_EFFECT.registry().codec.fieldOf("type").forGetter { i -> i.effectType },
                Codec.INT.optionalFieldOf("duration",0).forGetter { i -> i.duration },
                Codec.INT.optionalFieldOf("amplifier",0).forGetter { i -> i.amplifier },
                Codec.BOOL.optionalFieldOf("ambient",false).forGetter { i -> i.isAmbient },
                Codec.BOOL.optionalFieldOf("show_particles",true).forGetter { i -> i.shouldShowParticles() },
                Codec.BOOL.optionalFieldOf("show_icon",false).forGetter { i -> i.shouldShowIcon() }
            ).apply(instance){t,d,a,m,p,i -> StatusEffectInstance(t,d,a,m,p,i) }
        }

        val RANDOM_CHANCE = register(RandomChanceModifierConsumer.Type, Identifier(FC.MOD_ID,"chance"))
        val STATUS_EFFECT = register(StatusEffectModifierConsumer.Type, Identifier(FC.MOD_ID,"status"))
        val STATUS_EFFECT_STACKING = register(StackingStatusEffectModifierConsumer.Type, Identifier(FC.MOD_ID,"status_stacking"))
        val LIST = register(ListModifierConsumer.Type, Identifier(FC.MOD_ID,"list"))
        val PREDICATE = register(PredicateModifierConsumer.Type, Identifier(FC.MOD_ID,"predicate"))

    }

}