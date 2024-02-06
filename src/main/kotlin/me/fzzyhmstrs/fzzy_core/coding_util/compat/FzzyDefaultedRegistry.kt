package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

open class FzzyDefaultedRegistry<T>(registry: Registry<T>): FzzyRegistry<T>(registry) {

    override fun getId(var1: T): Identifier {
        return super.getId(var1)!!
    }

    override fun get(id: Identifier?): T {
        return super.get(id)!!
    }

    override fun get(index: Int): T {
        return super.get(index)!!
    }

}