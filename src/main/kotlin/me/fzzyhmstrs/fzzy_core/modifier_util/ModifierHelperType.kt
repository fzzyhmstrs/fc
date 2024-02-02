package me.fzzyhmstrs.fzzy_core.modifier_util

import me.fzzyhmstrs.fzzy_core.FC
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

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

    open fun add(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
        //println("Adding modifiers to ${modifierContainer.livingEntity} from stack $stack")
        for (mod in helper.modifiersFromNbt(stack)) {
            modifierContainer.addModifier(mod, this)
        }
    }

    open fun remove(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
        for (mod in helper.modifiersFromNbt(stack)) {
            modifierContainer.removeModifier(mod, this)
        }
    }

    open fun areModifiersEqual(old: ItemStack, young: ItemStack): Boolean{
        return helper.modifiersAreEqual(old, young)
    }

    abstract fun getModifierIdKey(): String

    abstract fun getModifiersKey(): String

    abstract fun getModifierInitKey(): String

    //for API compat
    fun initializeModifiers(stack: ItemStack, @Suppress("UNUSED_PARAMETER") nbtCompound: NbtCompound, @Suppress("UNUSED_PARAMETER") defaultMods: List<Identifier>){
        getModifierInitializer().initializeModifiers(stack)
    }

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
        val REGISTRY = FzzyPort.simpleRegistry<ModifierHelperType<*>>(Identifier(FC.MOD_ID,"modifier_helper_type"))
        val EMPTY_TYPE = Registry.register(REGISTRY,EmptyType.id,EmptyType)

        fun<T: AbstractModifier<T>> register(type: ModifierHelperType<T>): ModifierHelperType<T>{
            return Registry.register(REGISTRY,type.id,type)
        }

        fun add(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
            for (type in REGISTRY){
                type.add(stack, id, modifierContainer)
            }
        }
        fun remove(stack: ItemStack, id: SlotId, modifierContainer: ModifierContainer){
            for (type in REGISTRY){
                type.remove(stack, id, modifierContainer)
            }
        }

        fun areModifiersEqual(old: ItemStack, young: ItemStack): Boolean{
            for (type in REGISTRY){
                if (!type.areModifiersEqual(old, young)) return false
            }
            return true
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

            override fun getModifierInitKey(): String {
                return "empty_init"
            }

        }
    }

}
