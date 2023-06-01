package me.fzzyhmstrs.fzzy_core.coding_util

import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Utilities for implementing a persistent effect that isn't linked to a particular object. Once initiated, the object that called it does not necessarily have to continue existing or performing the action that initiated the persistent effect. This is useful in instances where you want an effect to occur over time, but don't want to deal with the headache of coding and managing tracking that persistent effect. All of that is handled here.
 */
object PersistentEffectHelper {

    private val persistentEffects: Vector<PersistentEffectInstance> = Vector()
    private val DUSTBIN = Dustbin { instance: PersistentEffectInstance ->
            persistentEffects.remove(instance); if (persistentEffects.isEmpty()) {
            persistentEffectsFlag.set(false)
        }
    }
    private var persistentEffectsFlag: AtomicBoolean = AtomicBoolean(false)

    /**
     * primary function of interest. Basically registers a persistent effect with the built-in ticker for execution. You can pass custom implementations of [PersistentEffectData] and [PersistentEffect] to the parameters as needed.
     *
     * Successful runtime can be achieved with a simple type check on the persistentEffect call.
     */
    fun setPersistentTickerNeed(
        effect: PersistentEffect,
        delay: Int, duration: Int,
        data: PersistentEffectData
    ){
        persistentEffects.add(PersistentEffectInstance(EventRegistry.Ticker(delay), delay, duration, effect, data))
        persistentEffectsFlag.set(true)
    }

    fun registerServer(){
        ServerLifecycleEvents.SERVER_STOPPING.register {
            flushPersistentEffects()
        }
    }

    private fun flushPersistentEffects(){
        while(persistentEffectsFlag.get()){
            persistentEffectTicker()
        }
    }

    internal fun persistentEffectTicker(){
        DUSTBIN.clean()
        if (!persistentEffectsFlag.get()) {
            return
        }
        for (i in 0 until persistentEffects.size) {
            val it = persistentEffects[i]
            it.ticker.tickUp()
            if (it.ticker.isReady()){
                val aug = it.augment
                aug.persistentEffect(it.data)
                val newDur = it.duration - it.delay
                if (newDur <= 0){
                    DUSTBIN.markDirty(it)
                } else {
                    it.duration = newDur
                }
            }
        }
    }

    private data class PersistentEffectInstance(val ticker: EventRegistry.Ticker, val delay: Int, var duration: Int, val augment: PersistentEffect, val data: PersistentEffectData)

    interface PersistentEffect {

        val delay: PerLvlI

        fun persistentEffect(data: PersistentEffectData)
    }
    
    interface PersistentEffectData{
        
    }
}
