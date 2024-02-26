package me.fzzyhmstrs.fzzy_core.modifier_util

import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

interface ModifierPredicate {

    fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean

    fun getType(): ModifierPredicateType<*>

}