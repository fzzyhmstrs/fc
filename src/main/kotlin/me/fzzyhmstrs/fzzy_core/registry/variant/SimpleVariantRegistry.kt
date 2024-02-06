package me.fzzyhmstrs.fzzy_core.registry.variant

import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import java.util.function.Function

open class SimpleVariantRegistry<T: Variant>(registry: Registry<T>, private val variantFactory: Function<Identifier,T>): VariantRegistry<T>(registry) {

    fun registerVariant(id: Identifier, texture: Identifier, randomlySelectable: Boolean = true): T{
        return this.register(id, variantFactory.apply(texture)).also { if(randomlySelectable) RANDOMLY_SELECTABLE.add(it) }
    }

    fun registerWeighted(id: Identifier, texture: Identifier, weight: Int): T{
        return this.register(id, variantFactory.apply(texture)).also { for (i in 1..weight) RANDOMLY_SELECTABLE.add(it) }
    }
}