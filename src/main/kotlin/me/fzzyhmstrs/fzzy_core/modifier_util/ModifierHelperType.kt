package me.fzzyhmstrs.fzzy_core.modifier_util

import me.fzzyhmstrs.fzzy_core.FC
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

abstract class ModifierHelperType(val id: Identifier) {

    abstract fun getModifierInitializer(): ModifierInitializer
    
    abstract fun getModifierIdKey(): String

    abstract fun getModifiersKey(): String

    open fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, defaultMods: List<Identifier>){
        getModifierInitializer().initializeModifiers(stack,nbt,defaultMods)
    }

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other !is ModifierHelperType) return false
        return other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    companion object{
        val REGISTRY = FabricRegistryBuilder.createDefaulted(ModifierHelperType::class.java, Identifier(FC.MOD_ID,"modifier_helper_type"),
            EmptyType.id).buildAndRegister()
        val EMPTY_TYPE = Registry.register(REGISTRY,EmptyType.id,EmptyType)

        fun register(type: ModifierHelperType): ModifierHelperType{
            return Registry.register(REGISTRY,type.id,type)
        }

        object EmptyType: ModifierHelperType(Identifier(FC.MOD_ID,"empty_helper")){
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