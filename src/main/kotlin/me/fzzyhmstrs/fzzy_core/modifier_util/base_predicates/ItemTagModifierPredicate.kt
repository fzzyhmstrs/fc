package me.fzzyhmstrs.fzzy_core.modifier_util.base_predicates

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierPredicate
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierPredicateType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class ItemTagModifierPredicate(private val tag:TagKey<Item>): ModifierPredicate {
    override fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean {
        if (stack == null) return false
        return isInTag(stack.item, tag)
    }

    private fun isInTag(item: Item,tag: TagKey<Item>): Boolean{
        return FzzyPort.ITEM.getEntry(item).isIn(tag)
    }

    override fun getType(): ModifierPredicateType<*> {
        return Type
    }

    companion object Type: ModifierPredicateType<ItemTagModifierPredicate> {

        private val CODEC = TagKey.codec(FzzyPort.ITEM.registry().key).xmap(
            {tag -> ItemTagModifierPredicate(tag)},
            {predicate -> predicate.tag}
        )

        override fun codec(): Codec<ItemTagModifierPredicate> {
            return CODEC
        }
    }

}