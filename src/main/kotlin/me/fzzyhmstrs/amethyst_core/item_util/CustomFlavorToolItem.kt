package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.item_util.interfaces.Flavorful
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.text.Text
import net.minecraft.world.World

/**
 * See [CustomFlavorItem]
 *
 * Version of that for ToolItems
 */
open class CustomFlavorToolItem(material: ToolMaterial,settings: Settings) : ToolItem(material, settings), Flavorful<CustomFlavorToolItem> {

    override var glint: Boolean = false
    override var flavor: String = ""
    override var flavorDesc: String = ""
    
    private val flavorText: MutableText by Lazy{
        makeFlavorText()
    }
    
    private val flavorTextDesc: MutableText by Lazy{
        makeFlavorTextDesc()
    }
    
    private fun makeFlavorText(): MutableText{
        val id = Registry.ITEM.getId(this)
        return AcText.translatable("item.${id.namespace}.${id.path}.flavor").formatted(Formatting.WHITE, Formatting.ITALIC)
    }
    
    private fun makeFlavorTextDesc(): MutableText{
        val id = Registry.ITEM.getId(this)
        return AcText.translatable("item.${id.namespace}.${id.path}.flavor.desc").formatted(Formatting.WHITE)
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

    override fun getFlavorItem(): CustomFlavorToolItem {
        return this
    }
}
