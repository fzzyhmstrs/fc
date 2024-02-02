@file:Suppress("unused")

package me.fzzyhmstrs.fzzy_core.coding_util

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.Material

object FzzyBlockSettings {
    /**
     * For basic materials with no special properties, just a color. Modder should specify their own color in most cases anyways
     */
    fun basic(): FabricBlockSettings {return FabricBlockSettings.of(Material.SOIL)}

    /*
    nonsold
    burn
    light
    destroyable
    movemnt
    replaceable
    blocksPistins
     */

    fun air(): FabricBlockSettings { return FabricBlockSettings.of(Material.AIR) }
    fun nonSolidLightMoveBarrier(): FabricBlockSettings { return FabricBlockSettings.of(Material.PORTAL)}
    fun nonSolidBurnLightMove(): FabricBlockSettings { return FabricBlockSettings.of(Material.CARPET)}
    fun nonSolidLightDestroyMove(): FabricBlockSettings { return FabricBlockSettings.of(Material.PLANT)}
    fun nonSolidLightDestroyMoveReplace(): FabricBlockSettings { return FabricBlockSettings.of(Material.REPLACEABLE_UNDERWATER_PLANT)}
    fun nonSolidBurnLightDestroyMoveReplace(): FabricBlockSettings { return FabricBlockSettings.of(Material.REPLACEABLE_PLANT)}
    fun liquid(): FabricBlockSettings { return FabricBlockSettings.of(Material.WATER)}
    fun lightDestroyMove(): FabricBlockSettings { return FabricBlockSettings.of(Material.COBWEB)}
    fun burn(): FabricBlockSettings { return FabricBlockSettings.of(Material.WOOD)}
    fun burnLight(): FabricBlockSettings { return FabricBlockSettings.of(Material.TNT)}
    fun burnLightDestroy(): FabricBlockSettings { return FabricBlockSettings.of(Material.LEAVES)}
    fun burnDestroyMove(): FabricBlockSettings { return FabricBlockSettings.of(Material.BAMBOO_SAPLING)}
    fun burnDestroy(): FabricBlockSettings { return FabricBlockSettings.of(Material.BAMBOO)}
    fun destroy(): FabricBlockSettings { return FabricBlockSettings.of(Material.MOSS_BLOCK)}
    fun lightDestroy(): FabricBlockSettings { return FabricBlockSettings.of(Material.CACTUS)}
    fun light(): FabricBlockSettings { return FabricBlockSettings.of(Material.GLASS)}
    fun barrier(): FabricBlockSettings { return FabricBlockSettings.of(Material.BARRIER)}
    fun nonSolidMove(): FabricBlockSettings { return FabricBlockSettings.of(Material.POWDER_SNOW)}
}