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
abstract class AbstractModifier<T: AbstractModifier<T>>(val modifierId: Identifier): Addable<T> {

    /**
     * Defines the descendant, if any for the modifier, and the lineage of the modifier family.
     *
     * See the [wiki](https://github.com/fzzyhmstrs/ac/wiki/Modifier-Framework) for details.
     */
    private var descendant: Identifier = AbstractModifierHelper.BLANK
    private var ancestor: Identifier = AbstractModifierHelper.BLANK

    private var objectsToAffect: Predicate<Identifier>? = null

    /**
     * called to access a type-specific compiler.
     */
    abstract fun compiler(): Compiler

    abstract fun getModifierHelper(): AbstractModifierHelper<T>

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
        return descendant != AbstractModifierHelper.BLANK
    }
    fun hasAncestor(): Boolean {
        return ancestor != AbstractModifierHelper.BLANK
    }
    /*fun getAncestor(): Identifier{
        return ancestor
    }*/
    fun getAncestor(): T?{
        return getModifierHelper().getModifierByType(ancestor)
    }
    fun getDescendant(): T?{
        return getModifierHelper().getModifierByType(descendant)
    }
    fun addDescendant(modifier: AbstractModifier<T>){
        descendant = modifier.modifierId
        modifier.ancestor = this.modifierId
    }
    fun addDescendant(modifier: Identifier){
        descendant = modifier
    }
    open fun hasObjectToAffect(): Boolean{
        return objectsToAffect != null
    }
    open fun addObjectToAffect(predicate: Predicate<Identifier>){
        objectsToAffect = predicate
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

    override fun toString(): String {
        return "[${this::class.java.simpleName}: $modifierId]"
    }

    override fun hashCode(): Int {
        return modifierId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is AbstractModifier<*>) return false
        return other.modifierId == modifierId
    }

    class CompiledModifiers<T: AbstractModifier<T>>(val modifiers: ArrayList<T>, val compiledData: T){
        fun combineWith(other: CompiledModifiers<T>, blank: T): CompiledModifiers<T>{
            val list: ArrayList<T> = arrayListOf()
            list.addAll(modifiers)
            list.addAll(other.modifiers)
            blank.plus(compiledData)
            blank.plus(other.compiledData)
            return CompiledModifiers(list,blank)
        }

        override fun toString(): String {
            return "CompiledModifier[components:$modifiers, compiled:$compiledData]"
        }
    }

    inner class Compiler(private val modifiers: ArrayList<T>, private val compiledData: T){

        fun add(modifier: T){
            modifiers.add(modifier)
            compiledData.plus(modifier)
        }

        fun compile(): CompiledModifiers<T>{
            return CompiledModifiers(modifiers, compiledData)
        }

    }
}