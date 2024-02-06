package me.fzzyhmstrs.fzzy_core.registry.variant

import me.fzzyhmstrs.fzzy_core.coding_util.compat.FzzyDefaultedRegistry
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

@Suppress("unused")
open class VariantRegistry<T: Variant>(registry: Registry<T>): FzzyDefaultedRegistry<T>(registry) {

    protected val RANDOMLY_SELECTABLE: MutableList<T> = mutableListOf()
    fun registerVariant(id: Identifier, variant: T, randomlySelectable: Boolean = true): T{
        return this.register(id, variant.also { if(randomlySelectable) RANDOMLY_SELECTABLE.add(it) })
    }

    fun registerWeighted(id: Identifier, variant: T, weight: Int): T{
        return this.register(id, variant.also { for (i in 1..weight) RANDOMLY_SELECTABLE.add(it) })
    }

    fun randomVariant(): T?{
        return if (RANDOMLY_SELECTABLE.isEmpty()) null else RANDOMLY_SELECTABLE.random()
    }

    fun writeNbt(nbt: NbtCompound, variant: T){
        nbt.putString(registry.key.value.toString(),getId(variant).toString())
    }

    fun readNbt(nbt: NbtCompound): T{
        return get(Identifier.tryParse(nbt.getString(registry.key.value.toString())))
    }

}