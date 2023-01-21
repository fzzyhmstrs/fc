package me.fzzyhmstrs.fzzy_core.mana_util

import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import net.minecraft.item.ItemStack

/**
 * Helper object for mana items that self-heal. A simple initialization method that maps the provided stack to a [Ticker][me.fzzyhmstrs.fzzy_core.registry.EventRegistry.Ticker]
 */
object ManaHelper {

    private val healTickers: MutableMap<Long, EventRegistry.Ticker> = mutableMapOf()

    /**
     * call this to add the ManaItem stack into the healing queue. Will not automatically heal the stack with this call.
     */
    fun initializeManaItem(stack: ItemStack){
        val id = Nbt.makeItemStackId(stack)
        if (!healTickers.containsKey(id)){
            val item = stack.item
            if (item is ManaItem) {
                healTickers[id] = EventRegistry.Ticker(item.getRepairTime())
            }
        }
    }

    /**
     * add a call to this method in the items tick to know when to use [ManaItem.healDamage] as needed in your implementation.
     */
    fun tickHeal(stack: ItemStack): Boolean{
        val id = Nbt.makeItemStackId(stack)
        val ticker = healTickers[id]?:return false
        ticker.tickUp()
        return ticker.isReady()
    }

    fun needsInitialization(stack: ItemStack): Boolean{
        val id = Nbt.getItemStackId(stack)
        return !healTickers.containsKey(id)
    }

}