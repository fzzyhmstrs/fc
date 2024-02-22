package me.fzzyhmstrs.fzzy_core.coding_util

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import me.fzzyhmstrs.fzzy_core.trinket_util.base_augments.AbstractEquipmentAugment
import me.fzzyhmstrs.fzzy_core.trinket_util.base_augments.AbstractPassiveAugment
import net.minecraft.advancement.Advancement
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.enchantment.Enchantment
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier
import java.util.function.Predicate

class AdvancementCompletionCriterion(private val id: Identifier): AbstractCriterion<AdvancementCompletionCriterion.AdvancementConditions>() {

    override fun getId(): Identifier {
        return id
    }

    override fun conditionsFromJson(
        obj: JsonObject,
        playerPredicate: LootContextPredicate,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
    ): AdvancementConditions {
        if (obj.has("advancement")){
            val el = obj.get("advancement")
            if (el.isJsonPrimitive){
                val str = el.asString
                val advancement = Identifier.tryParse(str)?: throw IllegalStateException("Advancement not a proper Identifier string in json object: ${obj.asString}.")
                return AdvancementConditions(id, { advancement == it }, playerPredicate)

            }else {
                throw IllegalStateException("Advancement Completion Criterion 'advancement' string not properly formatted in json object: ${obj.asString}")
            }
        } else if (obj.has("advancements")){
            val el = obj.get("advancements")
            if (el.isJsonArray){
                val list = el.asJsonArray.mapNotNull { Identifier.tryParse(try{it.asString} catch (e: Exception){null}) }
                return AdvancementConditions(id, { list.contains(it) }, playerPredicate)
            } else {
                throw IllegalStateException("Advancement Completion Criterion 'advancements' array not properly formatted in json object: ${obj.asString}")
            }
        } else {
            throw IllegalStateException("Advancement Completion Criterion missing condition in json object: ${obj.asString}")
        }
    }

    fun trigger(player: ServerPlayerEntity, advancement: Advancement){
        this.trigger(player) { condition -> condition.test(advancement) }
    }


    class AdvancementConditions(id: Identifier, private val advPredicate: Predicate<Identifier>, entityPredicate: LootContextPredicate): AbstractCriterionConditions(id,entityPredicate){

        fun test(advancement: Advancement): Boolean{
            return advPredicate.test(advancement.id)
        }

    }
}