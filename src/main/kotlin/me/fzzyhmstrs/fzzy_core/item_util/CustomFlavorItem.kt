package me.fzzyhmstrs.fzzy_core.item_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.item_util.interfaces.Flavorful
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import net.minecraft.world.World

/**
 * a simple item with the added functionality to display a "flavor text" and optionally a plain text description of what the flavor is depicting.
 *
 * also provides a method for manually setting a glint in an item.
 */
open class CustomFlavorItem(settings: Settings) : Item(settings), Flavorful<CustomFlavorItem> {

    override var glint = false
    override var flavor: String = ""
    override var flavorDesc: String = ""

    override fun withGlint(): CustomFlavorItem {
        return super.withGlint()
    }

    override fun withFlavor(flavorPath: String): CustomFlavorItem {
        return super.withFlavor(flavorPath)
    }

    override fun withFlavorDesc(flavorPath: String): CustomFlavorItem {
        return super.withFlavorDesc(flavorPath)
    }

    override fun withFlavorDefaultPath(id: Identifier): CustomFlavorItem {
        return super.withFlavorDefaultPath(id)
    }

    override fun withFlavorDescDefaultPath(id: Identifier): CustomFlavorItem {
        return super.withFlavorDescDefaultPath(id)
    }

    override fun addFlavorText(tooltip: MutableList<Text>, context: TooltipContext) {
        super.addFlavorText(tooltip, context)
    }

    private val flavorText: MutableText by lazy{
        makeFlavorText()
    }
    
    private val flavorTextDesc: MutableText by lazy{
        makeFlavorTextDesc()
    }
    
    private fun makeFlavorText(): MutableText{
        val id = Registry.ITEM.getId(this)
        val key = "item.${id.namespace}.${id.path}.flavor"
        val text = AcText.translatable(key).formatted(Formatting.WHITE, Formatting.ITALIC)
        if (text.string == key) return AcText.empty()
        return text
    }
    
    private fun makeFlavorTextDesc(): MutableText{
        val id = Registry.ITEM.getId(this)
        val key = "item.${id.namespace}.${id.path}.flavor.desc"
        val text = AcText.translatable(key).formatted(Formatting.WHITE)
        if (text.string == key) return AcText.empty()
        return text
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        addFlavorText(tooltip, context)
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return if (glint) {
            true
        } else {
            super.hasGlint(stack)
        }
    }
    
    override fun flavorText(): MutableText{
        return flavorText
    }
    override fun flavorDescText(): MutableText{
        return flavorTextDesc
    }

    override fun getFlavorItem(): CustomFlavorItem {
        return this
    }
}
