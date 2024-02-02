package me.fzzyhmstrs.fzzy_core.item_util

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtHelper
import net.minecraft.nbt.StringNbtReader
import net.minecraft.recipe.ShapedRecipe
import net.minecraft.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.JsonHelper

class FzzyIngredient private constructor(private val checks: List<Checker>){
    
    fun test(stack: ItemStack): Boolean{
        for (check in checks){
            if (check.check(stack)) return true
        }
        return false
    }

    override fun toString(): String{
        var result = "FzzyIngredient["
        for (check in checks){
            result = "$result{$check}"
        }
        result = "$result]"
        return result
    }

    companion object{

        fun outputFromJson(json: JsonObject): ItemStack {
            val item = ShapedRecipe.getItem(json)
            val i = JsonHelper.getInt(json, "count", 1)
            if (i < 1) {
                throw JsonSyntaxException("Invalid output count: $i")
            }
            if (json.has("nbt")) {
                try {
                    val nbtString = json.getAsJsonPrimitive("nbt").asString
                    val nbt = StringNbtReader.parse(nbtString)
                    return ItemStack(item, i).also { it.nbt = nbt }
                } catch (e: Exception){
                    FC.LOGGER.error("Invalid 'data' string in recipe json")
                    e.printStackTrace()
                }
            }
            return ItemStack(item, i)
        }

        fun fromJson(json: JsonElement): FzzyIngredient{
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
                        throw IllegalStateException("Improperly formatted FzzyIngredient. JsonArray has illegal member type, needs to be a string or a JsonObject: $jsonArray")
                    }
                }
                return FzzyIngredient(checks)
            } else if (json.isJsonPrimitive){
                val jsonPrimitive = json.asJsonPrimitive
                return FzzyIngredient(listOf(checkerFromPrimitive(jsonPrimitive)))
            }
            throw IllegalStateException("Improperly formatted FzzyIngredient. Needs to be a JsonObject, JsonArray, or Identifier string: $json")
        }

        private fun checkerFromObject(jsonObject: JsonObject): Checker{
            if (jsonObject.has("item")){
                val itemString = jsonObject.getAsJsonPrimitive("item").asString
                val itemId = Identifier.tryParse(itemString) ?: throw IllegalStateException("Invalid Identifier string in the 'item' member of a SetIngredient object.")
                if (jsonObject.has("nbt")){
                    val nbtString = jsonObject.getAsJsonPrimitive("nbt").asString
                    val nbt = StringNbtReader.parse(nbtString)
                    return ItemChecker(itemId,nbt)
                }
                return ItemChecker(itemId, NbtCompound())
            }else if (jsonObject.has("tag")){
                val tagString = jsonObject.getAsJsonPrimitive("tag").asString
                val tagId = Identifier.tryParse(tagString) ?: throw IllegalStateException("Invalid Identifier string in the 'tag' member of a SetIngredient object.")
                if (jsonObject.has("nbt")){
                    val nbtString = jsonObject.getAsJsonPrimitive("nbt").asString
                    val nbt = StringNbtReader.parse(nbtString)
                    return TagChecker(tagId, nbt)
                }
                return TagChecker(tagId,NbtCompound())
            } else {
                throw IllegalStateException("Expecting 'item' or 'tag' member in the SetIngredient object")
            }
        }

        private fun checkerFromPrimitive(json: JsonPrimitive): Checker{
            val jsonString = json.asString
            if (jsonString.isEmpty()) throw IllegalStateException("Error in FzzyIngredient: Empty item or tag string")
            return if (jsonString[0] == '#'){
                val tagString = jsonString.substring(1)
                val tagId = Identifier.tryParse(tagString) ?: throw IllegalStateException("Error in FzzyIngredient: Unparseable tag identifier: $tagString")
                TagChecker(tagId, NbtCompound())
            } else {
                val itemId = Identifier.tryParse(jsonString) ?: throw IllegalStateException("Error in FzzyIngredient: Unparseable item identifier: $jsonString")
                ItemChecker(itemId,NbtCompound())
            }
        }
    }

    private class ItemChecker(private val item: Identifier, private val nbt: NbtCompound): Checker{

        private val itemCached: Item by lazy{
            FzzyPort.ITEM.get(item)
        }
        
        override fun check(stack: ItemStack): Boolean{
            if(!stack.isOf(itemCached)) return false
            val stackNbt = stack.nbt ?: return nbt.isEmpty
            return NbtHelper.matches(nbt,stackNbt,true)
        }

        override fun toString(): String{
            return "ItemChecker[item: $item, nbt: $nbt]"
        }
    }

    private class TagChecker(private val tag: Identifier, private val nbt: NbtCompound): Checker{

        private val tagCached: TagKey<Item> by lazy{
            FzzyPort.ITEM.tagOf(tag)
        }
        
        override fun check(stack: ItemStack): Boolean{
            if(!stack.isIn(tagCached)) return false
            val stackNbt = stack.nbt ?: return nbt.isEmpty
            return NbtHelper.matches(nbt,stackNbt,true)
        }

        override fun toString(): String{
            return "TagChecker[tag: $tag, nbt: $nbt]"
        }
    }
                
    private interface Checker{
        fun check(stack: ItemStack): Boolean
    }
}
