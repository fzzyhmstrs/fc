package me.fzzyhmstrs.fzzy_core.modifier_util.serialization.base_consumers

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierConsumer
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierConsumerType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

class ListModifierConsumer(private val consumers: List<ModifierConsumer>): ModifierConsumer {
    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        for (consumer in consumers)
            consumer.apply(stack, user, target)
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<ListModifierConsumer> {

        private val codec = Codec.list(ModifierConsumerType.CODEC).xmap(
            {l -> ListModifierConsumer(l) },
            {c -> c.consumers}
        )

        override fun codec(): Codec<ListModifierConsumer> {
            return codec
        }

    }
}