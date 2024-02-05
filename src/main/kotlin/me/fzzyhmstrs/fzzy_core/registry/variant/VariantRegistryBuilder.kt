package me.fzzyhmstrs.fzzy_core.registry.variant

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleDefaultedRegistry
import net.minecraft.util.Identifier
import java.util.function.Function

class VariantRegistryBuilder<T: Variant>(private val registryId: Identifier, private val defaultId: Identifier) {

    private var tracked: Boolean = false

    private var factory: Function<Identifier,T>? = null

    fun simple(variantFactory: Function<Identifier,T>): VariantRegistryBuilder<T>{
        this.factory = variantFactory
        return this
    }

    fun tracked(): VariantRegistryBuilder<T>{
        this.tracked = true
        return this
    }

    fun build(): VariantRegistry<T>{
        val registry: SimpleDefaultedRegistry<T> =  FabricRegistryBuilder.createDefaulted(
            RegistryKey.ofRegistry<T>(registryId), defaultId).buildAndRegister()
        if (tracked){
            val trackedData = TrackedDataHandler.of(registry).also { TrackedDataHandlerRegistry.register(it) }
            return factory?.let { SimpleTrackedVariantRegistry(registry, it, trackedData) } ?: TrackedVariantRegistry<T>(registry, trackedData)
        }
        return factory?.let { SimpleVariantRegistry<T>(registry, it) } ?: VariantRegistry(registry)
    }

}