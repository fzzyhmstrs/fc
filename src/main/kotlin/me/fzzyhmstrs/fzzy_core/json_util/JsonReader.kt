package me.fzzyhmstrs.fzzy_core.json_util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType.Companion.EmptyType.id
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.*

object JsonReader {

    fun readEntityAttribute(json: JsonElement): Pair<EntityAttribute,EntityAttributeModifier>{
        val attributeJson = json.asJsonObject
        val attribute = try {
            val attributeIdString = attributeJson.get("attribute").asString
            val attributeId = Identifier.tryParse(attributeIdString)
                ?: throw IllegalStateException("Entity Attribute Json has an attribute bonus with an invalid identifier value [$attributeIdString].")
            FzzyPort.ATTRIBUTE.get(attributeId)
                ?: throw IllegalStateException("Entity Attribute Json has an attribute bonus with an attribute value [$attributeIdString] that can't be found in the attribute registry.")
        } catch (e: Exception){
            throw IllegalStateException("Entity Attribute Json has an attribute bonus with aan invalid 'attribute' key [$attributeJson]. Missing or needs to be a valid identifier string.")
        }
        val uuid = UUID.randomUUID()
        val name = "${FzzyPort.ATTRIBUTE.getId(attribute)}_modifier"
        val value = try {
            attributeJson.get("amount").asDouble
        } catch (e: Exception){
            throw IllegalStateException("Entity Attribute Json has an attribute bonus with an invalid 'amount' key. Missing or needs to be a valid number.")
        }
        val operation = try {
            val operationString = attributeJson.get("operation").asString
            EntityAttributeModifier.Operation.valueOf(operationString)
        } catch (e: Exception){
            throw IllegalStateException("Entity Attribute Json has an attribute bonus with aan invalid 'operation' key. Missing or needs to be a valid number.")
        }
        val attributeModifier = EntityAttributeModifier(uuid,name,value,operation)
        return Pair(attribute,attributeModifier)
    }

    fun readIdentifier(json: JsonElement): Identifier {
        val modifierIdString = json.asString
        return Identifier.tryParse(modifierIdString)
            ?: throw IllegalStateException("Identifier Json has an invalid identifier value [$modifierIdString].")
    }

    fun <T: AbstractModifier<T>> readModifier(json: JsonElement, classType: Class<T>): T{
        return ModifierRegistry.getByType(readIdentifier(json), classType)
            ?: throw IllegalStateException("Modifier Json has a bonus with a modifier value [$json] that can't be found in the modifier registry.")
    }

    fun readModifier(json: JsonElement, ): AbstractModifier<*>{
        return ModifierRegistry.get(readIdentifier(json))
            ?: throw IllegalStateException("Modifier Json has a bonus with a modifier value [$json] that can't be found in the modifier registry.")
    }

    fun readFormatting(json: JsonObject,name: String, fallback: List<Formatting>): List<Formatting>{
        return if (json.has(name)) {
            try {
                json.getAsJsonArray(name)
                    .map { jsonElement -> jsonElement.asString }
                    .map { str -> Formatting.byName(str) }
                    .filterNotNull()
            } catch (e: Exception) {
                throw IllegalStateException("Gear Set [$id] has an 'active_formatting' member that isn't a properly formatted array.")
            }
        } else {
            listOf(Formatting.GOLD, Formatting.BOLD)
        }
    }
}
