package me.fzzyhmstrs.fzzy_core.modifier_util.base_consumers

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumer
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumerType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.util.math.random.Random

class ListModifierConsumer(private val consumers: List<ModifierConsumer>): ModifierConsumer {
    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        for (consumer in consumers)
            consumer.apply(stack, user, target)
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<ListModifierConsumer>{

        private val codec = Codec.list(ModifierConsumerType.CODEC).xmap(
            {l -> ListModifierConsumer(l)},
            {c -> c.consumers}
        )

        override fun codec(): Codec<ListModifierConsumer> {
            return codec
        }

    }
}