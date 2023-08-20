package me.fzzyhmstrs.fzzy_core.item_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.config.FcConfig
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry


object FlavorHelper {

    fun makeFlavorText(item: Item): MutableText {
        val id = Registry.ITEM.getId(item)
        val key = "item.${id.namespace}.${id.path}.flavor"
        val text = AcText.translatable(key).formatted(Formatting.WHITE, Formatting.ITALIC)
        if (text.string == key) return AcText.empty()
        return text
    }

    fun makeFlavorTextDesc(item: Item): MutableText {
        val id = Registry.ITEM.getId(item)
        val key = "item.${id.namespace}.${id.path}.flavor.desc"
        val text = AcText.translatable(key).formatted(Formatting.WHITE)
        if (text.string == key) return AcText.empty()
        return text
    }

    fun addFlavorText(tooltip: MutableList<Text>, context: TooltipContext, flavorText: Text, flavorDescText: Text){
        if (flavorText != AcText.empty()) {
            tooltip.add(flavorText)
        }
        if ((context.isAdvanced && FcConfig.flavors.showFlavorDescOnAdvanced) || FcConfig.flavors.showFlavorDesc){
            if (flavorDescText != AcText.empty()){
                tooltip.add(flavorDescText)
            }
        }
    }

}