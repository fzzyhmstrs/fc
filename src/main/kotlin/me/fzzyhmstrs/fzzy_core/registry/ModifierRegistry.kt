package me.fzzyhmstrs.fzzy_core.registry

import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.function.SetNbtLootFunction
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtList
import net.minecraft.util.Identifier

/**
 * registers Modifiers. Comes with a short list of default modifiers and debugging modifiers for use with Augment-style Scepters
 *
 * This registry accepts any modifier based on the [AbstractModifier] system, and provides methods for interacting with specific Modifier types.
 */

@Suppress("MemberVisibilityCanBePrivate")
object ModifierRegistry {
    private val registry: MutableMap<Identifier, AbstractModifier<*>> = mutableMapOf()

    internal fun registerAll(){
    }

    /**
     * register a modifier with this.
     */
    fun register(modifier: AbstractModifier<*>){
        val id = modifier.modifierId
        if (registry.containsKey(id)){throw IllegalStateException("AbstractModifier with id $id already present in ModififerRegistry")}
        registry[id] = modifier
    }
    fun get(id: Identifier): AbstractModifier<*>?{
        return registry[id]
    }
    fun getByRawId(rawId: Int): AbstractModifier<*>?{
        return registry[getIdByRawId(rawId)]
    }
    fun getIdByRawId(rawId:Int): Identifier {
        return registry.keys.elementAtOrElse(rawId) { AbstractModifierHelper.BLANK }
    }
    fun getRawId(id: Identifier): Int{
        return registry.keys.indexOf(id)
    }
    fun isModifier(id: Identifier): Boolean{
        return this.get(id) != null
    }

    /**
     * get method that wraps in a type check, simplifying retrieval of only the relevant modifier type.
     */
    inline fun <reified T: AbstractModifier<T>> getByType(id: Identifier): T?{
        val mod = get(id)
        return if (mod is T){
            mod
        } else {
            null
        }
    }

    /**
     * Alternative get-by-type that does reflective class checking.
     */
    fun <T: AbstractModifier<T>>getByType(id: Identifier, classType: Class<T>): T?{
        val mod = get(id)
        return if (mod?.javaClass?.isInstance(classType) == true){
            try {
                mod as T
            } catch(e: ClassCastException){
                return null
            }
        } else {
            null
        }
    }

    /**
     * [LootFunction.Builder] usable with loot pool building that will add default modifiers, a provided list of modifiers, or both.
     */
    fun modifiersLootFunctionBuilder(item: Item, modifiers: List<AbstractModifier<*>> = listOf(), helper: AbstractModifierHelper<*>): LootFunction.Builder{
        val modList = NbtList()
        if (item is Modifiable) {
            if (item.defaultModifiers().isEmpty() && modifiers.isEmpty()){
                return SetEnchantmentsLootFunction.Builder() //empty builder for placehold purposes basically
            } else {
                item.defaultModifiers().forEach {
                    val nbtEl = NbtCompound()
                    nbtEl.putString(NbtKeys.MODIFIER_ID.str(),it.toString())
                    modList.add(nbtEl)
                }
                modifiers.forEach {
                    if (it.isAcceptableItem(ItemStack(item))) {
                        val nbtEl = NbtCompound()
                        nbtEl.putString(NbtKeys.MODIFIER_ID.str(), it.toString())
                        modList.add(nbtEl)
                    }
                }
            }
        } else if (modifiers.isEmpty()) {
            return SetEnchantmentsLootFunction.Builder()
        } else {
            modifiers.forEach {
                val nbtEl = NbtCompound()
                nbtEl.putString(NbtKeys.MODIFIER_ID.str(),it.toString())
                modList.add(nbtEl)
            }
        }
        val nbt = NbtCompound()
        nbt.put(NbtKeys.MODIFIERS.str(), modList)
        return SetNbtLootFunction.builder(nbt)
    }
}