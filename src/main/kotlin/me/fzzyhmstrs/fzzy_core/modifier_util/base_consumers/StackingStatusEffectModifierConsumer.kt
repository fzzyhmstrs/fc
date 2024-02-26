package me.fzzyhmstrs.fzzy_core.modifier_util.base_consumers

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumer
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumerType
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumerType.Types.statusEffectInstanceCodec
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.util.math.random.Random
import kotlin.math.max

class StackingStatusEffectModifierConsumer(private val user: Boolean, private val maxStacks: Int, private val statusEffect: StatusEffectInstance): ModifierConsumer {
    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        val entity = if(this.user){ user } else { target } ?: return
        if (entity.hasStatusEffect(statusEffect.effectType)){
            val effect = entity.getStatusEffect(statusEffect.effectType) ?: return
            val amp = max(effect.amplifier + 1, maxStacks - 1)
            val duration = max(effect.duration, statusEffect.duration)
            entity.addStatusEffect(StatusEffectInstance(statusEffect.effectType,amp,duration,statusEffect.isAmbient,statusEffect.shouldShowParticles(), statusEffect.shouldShowIcon()))
        } else {
            entity.addStatusEffect(StatusEffectInstance(statusEffect))
        }
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<StackingStatusEffectModifierConsumer>{

        private val codec = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<StackingStatusEffectModifierConsumer> ->
            instance.group(
                Codec.BOOL.optionalFieldOf("user", true).forGetter { c -> c.user },
                Codec.intRange(0, Int.MAX_VALUE).optionalFieldOf("max_stacks", -1).forGetter { c -> c.maxStacks },
                statusEffectInstanceCodec.fieldOf("status_effect").forGetter { c -> c.statusEffect }
            ).apply(instance){u,m,s -> StackingStatusEffectModifierConsumer(u,m,s)}
        }

        override fun codec(): Codec<StackingStatusEffectModifierConsumer> {
            return codec
        }

    }
}