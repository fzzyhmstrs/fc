package me.fzzyhmstrs.fzzy_core.item_util

import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.item.*

/**
 * Helper object to generate a list of all acceptable items in the Item Registry as applicable to various enchantment targets.
 */
object AcceptableItemStacks {

    private val targetMap: MutableMap<EnchantmentTarget,MutableList<ItemStack>> = mutableMapOf()

    fun baseAcceptableItemStacks(target: EnchantmentTarget?): MutableList<ItemStack>{
        return if (target == null){
            mutableListOf()
        } else {
            targetMap.computeIfAbsent(target){ targetGenerator(target) }
        }
    }

    private fun targetGenerator(target: EnchantmentTarget): MutableList<ItemStack>{
        val list: MutableList<ItemStack> = mutableListOf()
        return when(target){
            EnchantmentTarget.ARMOR->{
                for (entry in FzzyPort.ITEM){
                    if (entry is ArmorItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.ARMOR_FEET->{
                for (entry in FzzyPort.ITEM){
                    if (entry is ArmorItem && entry.slotType == EquipmentSlot.FEET){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.ARMOR_HEAD->{
                for (entry in FzzyPort.ITEM){
                    if (entry is ArmorItem && entry.slotType == EquipmentSlot.HEAD){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.ARMOR_CHEST->{
                for (entry in FzzyPort.ITEM){
                    if (entry is ArmorItem && entry.slotType == EquipmentSlot.CHEST){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.ARMOR_LEGS->{
                for (entry in FzzyPort.ITEM){
                    if (entry is ArmorItem && entry.slotType == EquipmentSlot.LEGS){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.CROSSBOW->{
                for (entry in FzzyPort.ITEM){
                    if (entry is CrossbowItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.BREAKABLE -> {
                for (entry in FzzyPort.ITEM){
                    if (entry.isDamageable){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.BOW->{
                for (entry in FzzyPort.ITEM){
                    if (entry is BowItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.VANISHABLE->{
                for (entry in FzzyPort.ITEM){
                    if (entry.isDamageable || entry is Vanishable || Block.getBlockFromItem(entry) is Vanishable){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.WEAPON->{
                for (entry in FzzyPort.ITEM){
                    if (entry is SwordItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.DIGGER->{
                for (entry in FzzyPort.ITEM){
                    if (entry is MiningToolItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.TRIDENT->{
                for (entry in FzzyPort.ITEM){
                    if (entry is TridentItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.FISHING_ROD->{
                for (entry in FzzyPort.ITEM){
                    if (entry is FishingRodItem){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
            EnchantmentTarget.WEARABLE->{
                for (entry in FzzyPort.ITEM){
                    if (entry is Equipment || Block.getBlockFromItem(entry) is Equipment){
                        list.add(ItemStack(entry,1))
                    }
                }
                list
            }
        }
    }
}
