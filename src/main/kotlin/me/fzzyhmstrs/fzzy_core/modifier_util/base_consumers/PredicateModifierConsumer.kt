package me.fzzyhmstrs.fzzy_core.modifier_util.base_consumers

import com.mojang.datafixers.util.Either
import com.mojang.datafixers.util.Pair
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.*
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random

class PredicateModifierConsumer private constructor (private val predicates: List<PredicateRecord>, private val consumer: ModifierConsumer): ModifierConsumer {

    constructor(consumer: ModifierConsumer, predicates: List<Pair<ModifierPredicate, Boolean>>): this(predicates.map { PredicateRecord(it.first,it.second) }, consumer)

    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        for (p in predicates){
            if (!p.predicate.test(AbstractModifierHelper.BLANK, if(p.user) user else target ?: user, stack))
                return
        }
        consumer.apply(stack, user, target)
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<PredicateModifierConsumer> {

        private val predicateCodec = Codec.either(
            Codec.list(PredicateRecord.CODEC),
            PredicateRecord.CODEC
        ).flatComapMap(
            { e -> e.map({ l -> l }, { r -> listOf(r) }) },
            { l -> if(l.isEmpty()) DataResult.error { "List can't be empty for PredicateModifierConsumer" } else if(l.size == 1) DataResult.success(Either.right<List<PredicateRecord>, PredicateRecord>(l[0])) else DataResult.success(Either.left<List<PredicateRecord>, PredicateRecord>(l)) }
        )

        private val codec = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PredicateModifierConsumer> ->
            instance.group(
                predicateCodec.fieldOf("predicates").forGetter { p -> p.predicates },
                ModifierConsumerType.CODEC.fieldOf("consumer").forGetter { p -> p.consumer }
            ).apply(instance) { p,c -> PredicateModifierConsumer(p,c) }
        }

        override fun codec(): Codec<PredicateModifierConsumer> {
            return codec
        }

    }

    private class PredicateRecord(val predicate: ModifierPredicate, val user: Boolean){
        companion object{
            val CODEC: Codec<PredicateRecord> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PredicateRecord> ->
                instance.group(
                    ModifierPredicateType.CODEC.fieldOf("predicate").forGetter { p -> p.predicate },
                    Codec.BOOL.optionalFieldOf("user", true).forGetter { p -> p.user }
                ).apply(instance){p,u -> PredicateRecord(p,u)}
            }
        }
    }
}