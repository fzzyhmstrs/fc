package me.fzzyhmstrs.fzzy_core.registry.variant

import net.minecraft.entity.data.TrackedDataHandler
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import java.util.function.Function

open class TrackedVariantRegistry<T: Variant>(registry: Registry<T>, private val trackedData: TrackedDataHandler<T>): VariantRegistry<T>(registry) {

    fun trackedDataHandler(): TrackedDataHandler<T>{
        return trackedData
    }

}