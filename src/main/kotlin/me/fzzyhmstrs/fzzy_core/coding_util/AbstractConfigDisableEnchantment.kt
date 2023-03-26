package me.fzzyhmstrs.fzzy_core.coding_util

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting


open class AbstractConfigDisableEnchantment(weight: Rarity, target: EnchantmentTarget, vararg slot: EquipmentSlot): Enchantment(weight, target, slot) {

    open fun checkEnabled(): Boolean {
        return true
    }

    fun isEnabled(): Boolean{
        return checkEnabled()
    }

    override fun getName(level: Int): Text {
        val baseText = super.getName(level) as MutableText
        if (!checkEnabled()) {
            return baseText
                .append(AcText.translatable("scepter.augment.disabled"))
                .formatted(Formatting.DARK_RED)
                .formatted(Formatting.STRIKETHROUGH)
        }
        return baseText
    }
}