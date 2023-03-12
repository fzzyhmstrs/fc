package me.fzzyhmstrs.fzzy_core.modifier_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.Addable
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier.CompiledModifiers
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Predicate

/**
 * The basis of the modifier system. Modifiers are an alternative to enchantments that internalize their functionality. See Piercing and Multishot in the [Crossbow Item][net.minecraft.item.CrossbowItem] for two examples of how Enchantments are externalized.
 *
 * See [AbstractModifierHelper] for information on building a helper to add remove, compile, and get modifiers for stacks.
 *
 * See the [wiki](https://github.com/fzzyhmstrs/ac/wiki/Modifier-Framework) for more details on implementation and usage.
 *
 * The underlying intention of a Modifier is:
 *
 * 1) Any modifier of a given type will perform its entire intended function with non-specific call(s). No special external implementation should be needed for any one Modifier.
 *
 * 2) Modifiers are [Addable]. The plus function adds together whichever members of a Modifier class for compilation
 *
 * 3) Modifiers are ["Compilable"][CompiledModifiers]. Any number of modifiers of a given type can be compiled together into a set of CompiledModifiers that a single point of contact can use to execute all relevant effects.
 */
abstract class AbstractModifier<T: Addable<T>>(val modifierId: Identifier): Addable<T> {

    /**
     * Defines the descendant, if any for the modifier, and the lineage of the modifier family.
     *
     * See the [wiki](https://github.com/fzzyhmstrs/ac/wiki/Modifier-Framework) for details.
     */
    private var descendant: Identifier = AbstractModifierHelper.BLANK
    private val lineage: List<Identifier> by lazy { generateLineage() }


    private var objectsToAffect: Predicate<Identifier>? = null

    private var hasDesc: Boolean = false
    private var hasObjectToAffect: Boolean = false

    /**
     * called to access a type-specific compiler.
     */
    abstract fun compiler(): Compiler

    abstract fun getModifierHelper(): AbstractModifierHelper<*>

    /**
     * defines the lang translation key for [TranslatableText][net.minecraft.text.Text.translatable].
     */
    open fun getTranslationKey(): String{
        return getModifierHelper().getTranslationKeyFromIdentifier(modifierId)
    }
    
    /**
     * defines the lang translation key for TranslatableText of the extended modifier description.
     */
    open fun getDescTranslationKey(): String{
        return getModifierHelper().getDescTranslationKeyFromIdentifier(modifierId)
    }

    open fun getTranslation(): MutableText{
        return AcText.translatable(getTranslationKey())
    }

    open fun getDescTranslation(): MutableText{
        return AcText.translatable(getDescTranslationKey())
    }

    open fun onAdd(stack: ItemStack){
    }

    open fun onRemove(stack: ItemStack){
    }

    fun hasDescendant(): Boolean{
        return hasDesc
    }
    fun addDescendant(modifier: AbstractModifier<T>){
        val id = modifier.modifierId
        descendant = id
        hasDesc = true
    }
    fun getModLineage(): List<Identifier>{
        return lineage
    }
    private fun generateLineage(): List<Identifier>{
        val nextInLineage = ModifierRegistry.get(descendant)
        val lineage: MutableList<Identifier> = mutableListOf(this.modifierId)
        lineage.addAll(nextInLineage?.getModLineage() ?: listOf())
        return lineage
    }
    open fun hasObjectToAffect(): Boolean{
        return hasObjectToAffect
    }
    open fun addObjectToAffect(predicate: Predicate<Identifier>){
        objectsToAffect = predicate
        hasObjectToAffect = true
    }
    open fun checkObjectsToAffect(id: Identifier): Boolean{
        return objectsToAffect?.test(id) ?: return false
    }
    open fun getName(): Text {
        return AcText.literal("$modifierId")
    }
    open fun isAcceptableItem(stack: ItemStack): Boolean{
        acceptableItemStacks().forEach {
            if (stack.isOf(it.item)){
                return true
            }
        }
        return false
    }
    open fun acceptableItemStacks(): MutableList<ItemStack>{
        return mutableListOf()
    }

    class CompiledModifiers<T: Addable<T>>(val modifiers: List<T>, val compiledData: T)

    inner class Compiler(private val modifiers: MutableList<T>, private val compiledData: T){

        fun add(modifier: T){
            modifiers.add(modifier)
            compiledData.plus(modifier)
        }

        fun compile(): CompiledModifiers<T>{
            return CompiledModifiers(modifiers, compiledData)
        }

    }
}
