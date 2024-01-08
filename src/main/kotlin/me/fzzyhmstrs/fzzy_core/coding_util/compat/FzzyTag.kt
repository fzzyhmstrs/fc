package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.registry.tag.TagKey

class FzzyTag<T>(private val registry: FzzyRegistry<T>,private val tagKey: TagKey<T>) {

    fun get(): TagKey<T>{
        return tagKey
    }

    fun contains(t: T): Boolean{
        return registry.isIn(this,t)
    }

}