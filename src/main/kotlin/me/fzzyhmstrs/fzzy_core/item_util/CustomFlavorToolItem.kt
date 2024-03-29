package me.fzzyhmstrs.fzzy_core.item_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.item_util.interfaces.Flavorful
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolItem
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.Registries
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
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

    override fun withGlint(): CustomFlavorToolItem {
        return super.withGlint()
    }

    override fun withFlavor(flavorPath: String): CustomFlavorToolItem {
        return super.withFlavor(flavorPath)
    }

    override fun withFlavorDesc(flavorPath: String): CustomFlavorToolItem {
        return super.withFlavorDesc(flavorPath)
    }

    override fun withFlavorDefaultPath(id: Identifier): CustomFlavorToolItem {
        return super.withFlavorDefaultPath(id)
    }

    override fun withFlavorDescDefaultPath(id: Identifier): CustomFlavorToolItem {
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
        val id = Registries.ITEM.getId(this)
        val key = "item.${id.namespace}.${id.path}.flavor"
        val text = AcText.translatable(key).formatted(Formatting.WHITE, Formatting.ITALIC)
        if (text.string == key) return AcText.empty()
        return text
    }

    private fun makeFlavorTextDesc(): MutableText{
        val id = Registries.ITEM.getId(this)
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

    override fun getFlavorItem(): CustomFlavorToolItem {
        return this
    }
}
