package me.fzzyhmstrs.fzzy_core.modifier_util

import net.minecraft.client.gui.tooltip.Tooltip
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Identifier

interface ModifierInitializer {
    fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, list: List<Identifier>)
    fun addModifierTooltip(stack: ItemStack,tooltip: MutableList<Text>, context: TooltipContext)
}