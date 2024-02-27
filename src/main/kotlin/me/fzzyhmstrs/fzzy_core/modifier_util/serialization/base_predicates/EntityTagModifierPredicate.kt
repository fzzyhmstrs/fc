package me.fzzyhmstrs.fzzy_core.modifier_util.serialization.base_predicates

import com.mojang.serialization.Codec
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicate
import me.fzzyhmstrs.fzzy_core.modifier_util.serialization.ModifierPredicateType
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

class EntityTagModifierPredicate(private val tag:TagKey<EntityType<*>>): ModifierPredicate {
    override fun test(toTest: Identifier, entity: LivingEntity?, stack: ItemStack?): Boolean {
        if (entity == null) return false
        return isInTag(entity.type, tag)
    }

    private fun isInTag(type: EntityType<*>,tag: TagKey<EntityType<*>>): Boolean{
        return FzzyPort.ENTITY_TYPE.getEntry(type).isIn(tag)
    }

    override fun getType(): ModifierPredicateType<*> {
        return Type
    }

    companion object Type: ModifierPredicateType<EntityTagModifierPredicate> {

        private val CODEC = TagKey.codec(FzzyPort.ENTITY_TYPE.registry().key).xmap(
            {tag -> EntityTagModifierPredicate(tag) },
            {predicate -> predicate.tag}
        )

        override fun codec(): Codec<EntityTagModifierPredicate> {
            return CODEC
        }
    }

}