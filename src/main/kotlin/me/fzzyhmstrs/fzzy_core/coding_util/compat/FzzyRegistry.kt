package me.fzzyhmstrs.fzzy_core.coding_util.compat

import com.mojang.serialization.DynamicOps
import net.minecraft.registry.Registry
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.minecraft.util.collection.IndexedIterable
import java.util.Optional
import java.util.stream.Stream

@Suppress("unused")
open class FzzyRegistry<T>(protected val registry: Registry<T>): IndexedIterable<T> {

    fun <U> keys(ops: DynamicOps<U>): Stream<U>{
        return registry.keys(ops)
    }

    open fun getId(var1: T): Identifier?{
        return registry.getId(var1)
    }

    fun registry(): Registry<T>{
        return registry
    }

    fun iterateEntries(tag: TagKey<T>): MutableIterable<RegistryEntry<T>>{
        return registry.iterateEntries(tag)
    }

    open fun get(id: Identifier?): T?{
        return registry.get(id)
    }

    fun getOrEmpty(id: Identifier?): Optional<T>{
        return registry.getOrEmpty(id)
    }

    fun getIds(): Set<Identifier>{
        return registry.ids
    }

    fun stream(): Stream<T>{
        return registry.stream()
    }

    fun containsId(id: Identifier): Boolean{
        return registry.containsId(id)
    }

    fun<V:T> register(id: Identifier, entry: V): V{
        return Registry.register(registry,id,entry)
    }

    fun tagOf(id: Identifier): TagKey<T>{
        return TagKey.of(registry.key,id)
    }

    fun isInTag(entry: T, tag: TagKey<T>): Boolean{
        return registry.getEntry(entry).isIn(tag)
    }

    fun getEntry(entry: T): RegistryEntry<T>{
        return registry.getEntry(entry)
    }

    override fun iterator(): MutableIterator<T> {
        return registry.iterator()
    }

    override fun getRawId(value: T): Int {
        return registry.getRawId(value)
    }

    override fun get(index: Int): T? {
        return registry[index]
    }

    override fun size(): Int {
        return registry.size()
    }


}