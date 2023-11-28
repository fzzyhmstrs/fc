package me.fzzyhmstrs.fzzy_core.mana_util

import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import kotlin.math.max
import kotlin.math.min

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

    fun getManaItems(user: PlayerEntity): MutableList<ItemStack>{
        val stacks: MutableList<ItemStack> = mutableListOf()
        for (stack2 in user.inventory.main){
            if (stack2.item is ManaItem && stack2.isDamaged){
                stacks.add(stack2)
            }
        } // iterate over the inventory and look for items that are interfaced with "ManaItem"
        for (stack2 in user.inventory.offHand){
            if (stack2.item is ManaItem && stack2.isDamaged){
                stacks.add(stack2)
            }
        }
        for (stack2 in user.inventory.armor){
            if (stack2.item is ManaItem && stack2.isDamaged){
                stacks.add(stack2)
            }
        }
        val stacks2 = TrinketChecker.getTrinketStacks(user)
        stacks2.forEach {
            if (it.item is ManaItem && it.isDamaged){
                stacks.add(it)
            }
        }
        return stacks
    }

    fun manaHealItems(list: MutableList<ItemStack>, world: World, healLeft: Int): Int{
        var hl = healLeft
        if (hl <= 0 || list.isEmpty()) return max(0,hl)
        val rnd = world.random.nextInt(list.size)
        val stack = list[rnd]
        val healAmount = min(5,hl)
        val healedAmount = (stack.item as? ManaItem)?.healDamage(healAmount,stack) ?: 0
        hl -= min(healAmount,healedAmount)
        if (!stack.isDamaged){
            list.remove(stack)
        }
        return manaHealItems(list,world,hl)
    }

}