package me.fzzyhmstrs.fzzy_core.registry.variant

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleDefaultedRegistry
import net.minecraft.util.Identifier
import java.util.function.Function

class VariantRegistryBuilder<T: Variant>(private val registryId: Identifier, private val defaultId: Identifier) {

    fun build(): VariantRegistry<T>{
        val registry: SimpleDefaultedRegistry<T> =  FabricRegistryBuilder.createDefaulted(
            RegistryKey.ofRegistry<T>(registryId), defaultId).buildAndRegister()
        return VariantRegistry(registry)
    }

    fun buildSimple(variantFactory: Function<Identifier,T>): SimpleVariantRegistry<T>{
        val registry: SimpleDefaultedRegistry<T> =  FabricRegistryBuilder.createDefaulted(
            RegistryKey.ofRegistry<T>(registryId), defaultId).buildAndRegister()
        return SimpleVariantRegistry<T>(registry, variantFactory)
    }

    fun buildTracked(): TrackedVariantRegistry<T>{
        val registry: SimpleDefaultedRegistry<T> =  FabricRegistryBuilder.createDefaulted(
            RegistryKey.ofRegistry<T>(registryId), defaultId).buildAndRegister()
        val trackedData = TrackedDataHandler.of(registry).also { TrackedDataHandlerRegistry.register(it) }
        return TrackedVariantRegistry<T>(registry, trackedData)
    }

    fun buildSimpleTracked(variantFactory: Function<Identifier,T>): SimpleTrackedVariantRegistry<T>{
        val registry: SimpleDefaultedRegistry<T> =  FabricRegistryBuilder.createDefaulted(
            RegistryKey.ofRegistry<T>(registryId), defaultId).buildAndRegister()
        val trackedData = TrackedDataHandler.of(registry).also { TrackedDataHandlerRegistry.register(it) }
        return SimpleTrackedVariantRegistry<T>(registry, variantFactory, trackedData)
    }
}