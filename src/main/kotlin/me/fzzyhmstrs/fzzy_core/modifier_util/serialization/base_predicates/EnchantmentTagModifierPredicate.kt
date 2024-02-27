package me.fzzyhmstrs.fzzy_core.modifier_util.serialization.base_predicates

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicate
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicateType
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class EnchantmentTagModifierPredicate(private val tag:TagKey<Enchantment>): ModifierPredicate {
    override fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean {
        return isInTag(toTest, tag)
    }

    private fun isInTag(id: Identifier,tag: TagKey<Enchantment>): Boolean{
        val augment = FzzyPort.ENCHANTMENT.get(id)?:return false
        return FzzyPort.ENCHANTMENT.getEntry(augment).isIn(tag)
    }

    override fun getType(): ModifierPredicateType<*> {
        return Type
    }

    companion object Type: ModifierPredicateType<EnchantmentTagModifierPredicate> {

        private val CODEC = TagKey.codec(FzzyPort.ENCHANTMENT.registry().key).xmap(
            {tag -> EnchantmentTagModifierPredicate(tag) },
            {predicate -> predicate.tag}
        )

        override fun codec(): Codec<EnchantmentTagModifierPredicate> {
            return CODEC
        }
    }

}