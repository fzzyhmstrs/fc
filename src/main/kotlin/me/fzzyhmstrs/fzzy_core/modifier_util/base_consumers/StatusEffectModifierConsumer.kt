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

class StatusEffectModifierConsumer(private val user: Boolean,private val statusEffect: StatusEffectInstance): ModifierConsumer {
    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (this.user)
            user.addStatusEffect(StatusEffectInstance(statusEffect))
        else
            target?.addStatusEffect(StatusEffectInstance(statusEffect))
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<StatusEffectModifierConsumer>{

        private val codec = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<StatusEffectModifierConsumer> ->
            instance.group(
                Codec.BOOL.optionalFieldOf("user", true).forGetter { c -> c.user },
                statusEffectInstanceCodec.fieldOf("status_effect").forGetter { c -> c.statusEffect }
            ).apply(instance){u,s -> StatusEffectModifierConsumer(u,s)}
        }

        override fun codec(): Codec<StatusEffectModifierConsumer> {
            return codec
        }

    }
}