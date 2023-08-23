package me.fzzyhmstrs.fzzy_core.item_util

import com.google.common.collect.ArrayListMultimap
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import net.minecraft.block.BlockState
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Ingredient
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class FzzyIngredient private constructor(private val checks: List<Checker>){
    
    fun test(stack: ItemStack){
        for (check in checks){
            if (check.check(stack)) return true
        }
        return false
    }

    companion object{

        fun fromJson(json: JsonElement): SetIngredient{                
            if (json.isJsonObject){
                val jsonObject = json.asJsonObject
                return FzzyIngredient(listOf(checkerFromObject(jsonObject)))  
            } else if (json.isJsonArray){
                val jsonArray = json.asJsonArray
                val checks: MutableList<Checker> = mutableListOf()
                for (jsonEl in jsonArray){
                    if (jsonEl.isJsonObject) {
                        val jsonObject = jsonEl.asJsonObject
                        checks.add(checkerFromObject(jsonObject))
                    } else if (jsonEl.isJsonPrimitive){
                        val jsonPrimitive = jsonEl.asJsonPrimitive
                        checks.add(checkerFromPrimitive(jsonPrimitive))
                    } else {
                        throw TODO()
                    }
                }
                return FzzyIngredient(checks)
            } else if (json.isJsonPrimitive){
                val jsonPrimitive = json.asJsonPrimitive
                return FzzyIngredient(listOf(checkerFromPrimitive(jsonObject)))  
            }
            throw IllegalStateException("Improperly formatted SetIngredient. Needs to be a JsonObject or JsonArray: $json")
        }
    }

    private fun checkerFromObject(json: JsonObject): Checker{
        if (jsonObject.has("item")){
            val itemString = json.getAsJsonPrimitive("item").asString
            val itemId = Identifier.tryParse(itemString) ?: IllegalStateException("Invalid Identifier string in the 'item' member of a SetIngredient object.")
            if jsonObject.has("nbt"){
                val nbtString = json.getAsJsonPrimitive("nbt").asString
                TODO()
            }
            return SetIngredient(listOf(ItemChecker(itemId)))
        }else if (json.has("tag")){
            val tagString = json.getAsJsonPrimitive("tag").asString
            val tagId = Identifier.tryParse(tagString) ?: IllegalStateException("Invalid Identifier string in the 'tag' member of a SetIngredient object.")
            if jsonObject.has("nbt"){
                val nbtString = json.getAsJsonPrimitive("nbt").asString
                TODO()
            }
        } else {
            throw IllegalStateException("Expecting 'item' or 'tag' member in the SetIngredient object")
        }        
    }

    private fun checkerFromPrimitive(json: JsonPrimitive): Checker{
        TODO()
    }

    private class ItemChecker(private val item: Identifier, private val nbt: NbtCompound){

        private val itemCached: Item by Lazy{
            Registries.ITEM.get(item)
        }
        
        override fun check(stack: ItemStack){
            if(!stack.isOf(itemCached)) return false
            val stackNbt = stack.nbt ?: return nbt.isEmpty
            for (key in nbt.getKeys){
                if (stackNbt.get(key) != stackNbt.get(key)) return false
            }
            return true
        }
    }

    private class ItemChecker(private val item: Identifier, private val nbt: NbtCompound){

        private val itemCached: Item by Lazy{
            Registries.ITEM.get(item)
        }
        
        override fun check(stack: ItemStack){
            if(!stack.isOf(itemCached)) return false
            val stackNbt = stack.nbt ?: return nbt.isEmpty
            for (key in nbt.getKeys){
                if (stackNbt.get(key) != stackNbt.get(key)) return false
            }
            return true
        }
    }

    private class TagChecker(private val tag: Identifier, private val nbt: NbtCompound){

        private val tagCached: TagKey<Item> by Lazy{
            TagKey.of(RegistryKeys.ITEM,tag)
        }
        
        override fun check(stack: ItemStack){
            if(!stack.isIn(tagCached)) return false
            val stackNbt = stack.nbt ?: return nbt.isEmpty
            for (key in nbt.getKeys){
                if (stackNbt.get(key) != nbt.get(key)) return false
            }
            return true
        }
    }
                
    private interface Checker{
        fun check(stack: ItemStack): Boolean
    }
}
