package me.fzzyhmstrs.fzzy_core.registry.variant

import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import java.util.function.Function

open class SimpleTrackedVariantRegistry<T: Variant>(registry: Registry<T>, variantFactory: Function<Identifier, T>, private val trackedData: TrackedDataHandler<T>): SimpleVariantRegistry<T>(registry, variantFactory) {

    fun trackedDataHandler(): TrackedDataHandler<T>{
        return trackedData
    }

}