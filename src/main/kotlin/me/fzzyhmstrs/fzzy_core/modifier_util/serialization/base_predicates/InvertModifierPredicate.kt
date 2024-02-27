package me.fzzyhmstrs.fzzy_core.modifier_util.serialization.base_predicates

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicate
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicateType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class InvertModifierPredicate(private val predicate: ModifierPredicate): ModifierPredicate {
    override fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean {
        return !predicate.test(toTest, entity, stack)
    }

    override fun getType(): ModifierPredicateType<*> {
        return Type
    }

    companion object Type: ModifierPredicateType<InvertModifierPredicate> {

        private val CODEC = ModifierPredicateType.CODEC.xmap(
            {p -> InvertModifierPredicate(p) },
            {i -> i.predicate}
        )

        override fun codec(): Codec<InvertModifierPredicate> {
            return CODEC
        }
    }

}