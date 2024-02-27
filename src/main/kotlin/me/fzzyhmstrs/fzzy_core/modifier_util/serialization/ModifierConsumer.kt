package me.fzzyhmstrs.fzzy_core.modifier_util.serialization

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

interface ModifierConsumer {

    fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)

    fun getType(): ModifierConsumerType<*>
}