package me.fzzyhmstrs.fzzy_core.modifier_util.base_predicates

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierPredicate
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierPredicateType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class OrModifierPredicate(private val predicates: List<ModifierPredicate>): ModifierPredicate {
    override fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean {
        for (predicate in predicates){
            if (predicate.test(toTest, entity, stack))
                return true
        }
        return false
    }

    override fun getType(): ModifierPredicateType<*> {
        return Type
    }

    companion object Type: ModifierPredicateType<OrModifierPredicate> {

        private val CODEC = Codec.list(ModifierPredicateType.CODEC).xmap(
            {p -> OrModifierPredicate(p)},
            {a -> a.predicates}
        )

        override fun codec(): Codec<OrModifierPredicate> {
            return CODEC
        }
    }

}