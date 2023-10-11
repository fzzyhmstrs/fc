package me.fzzyhmstrs.fzzy_core.modifier_util

import me.fzzyhmstrs.fzzy_core.FC
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier

abstract class ModifierHelperType <T: AbstractModifier<T>> (val id: Identifier, private val helper: AbstractModifierHelper<T>) {

    open fun getModifierInitializer(): ModifierInitializer {
        return helper
    }

    open fun helper(): AbstractModifierHelper<T>{
        return helper
    }

    open fun compile(input: List<Identifier>?, predicateId: Identifier?): AbstractModifier.CompiledModifiers<T> {
        return helper.compile(input, predicateId)
    }

    open fun add(stack: ItemStack, modifierContainer: ModifierContainer){
        for (mod in helper.getRelevantModifiers(modifierContainer.livingEntity, stack)) {
            modifierContainer.addModifier(mod, this)
        }
    }

    open fun remove(stack: ItemStack, modifierContainer: ModifierContainer){
        for (mod in helper.getRelevantModifiers(modifierContainer.livingEntity, stack)) {
            modifierContainer.removeModifier(mod, this)
        }
    }

    abstract fun getModifierIdKey(): String

    abstract fun getModifiersKey(): String

    open fun initializeModifiers(stack: ItemStack){
        getModifierInitializer().initializeModifiers(stack)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ModifierHelperType<*>) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object{
        val REGISTRY = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry<ModifierHelperType<*>>(Identifier(FC.MOD_ID,"modifier_helper_type"))).buildAndRegister()
        val EMPTY_TYPE = Registry.register(REGISTRY,EmptyType.id,EmptyType)

        fun<T: AbstractModifier<T>> register(type: ModifierHelperType<T>): ModifierHelperType<T>{
            return Registry.register(REGISTRY,type.id,type)
        }

        fun add(stack: ItemStack, modifierContainer: ModifierContainer){
            for (type in REGISTRY){
                type.add(stack, modifierContainer)
            }
        }
        fun remove(stack: ItemStack, modifierContainer: ModifierContainer){
            for (type in REGISTRY){
                type.remove(stack, modifierContainer)
            }
        }

        object EmptyType: ModifierHelperType<AbstractModifierHelper.Companion.EmptyModifier>(Identifier(FC.MOD_ID,"empty_helper"), AbstractModifierHelper.getEmptyHelper()){
            override fun getModifierInitializer(): ModifierInitializer {
                return AbstractModifierHelper.getEmptyHelper()
            }

            override fun getModifierIdKey(): String {
                return "empty_modifier_id"
            }

            override fun getModifiersKey(): String {
                return "empty_modifiers"
            }

        }
    }

}
