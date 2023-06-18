package me.fzzyhmstrs.fzzy_core.coding_util

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.block.piston.PistonBehavior

object FzzyBlockSettings {
    /**
     * For basic materials with no special properties, just a color. Modder should specify their own color in most cases anyways
     */
    fun basic(): FabricBlockSettings {return FabricBlockSettings.create()}

    /*
    nonsold
    burn
    light
    destroyable
    movemnt 
    replaceable
    blocksPistins
     */
    
    fun air(): FabricBlockSettings { return FabricBlockSettings.create().replaceable().noCollision().dropsNothing().air() }
    fun nonSolidLightMoveBarrier(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().pistonBehavior(PistonBehavior.BLOCK)}
    fun nonSolidBurnLightMove(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().burnable()}
    fun nonSolidLightDestroyMove(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().pistonBehavior(PistonBehavior.DESTROY)}
    fun nonSolidLightDestroyMoveReplace(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().pistonBehavior(PistonBehavior.DESTROY).replaceable()}
    fun nonSolidBurnLightDestroyMoveReplace(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().burnable().pistonBehavior(PistonBehavior.DESTROY).replaceable()}
    fun liquid(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().pistonBehavior(PistonBehavior.DESTROY).replaceable().liquid()}
    fun lightDestroyMove(): FabricBlockSettings { return FabricBlockSettings.create().noCollision().pistonBehavior(PistonBehavior.DESTROY)}
    fun burn(): FabricBlockSettings { return FabricBlockSettings.create().burnable()}
    fun burnLight(): FabricBlockSettings { return FabricBlockSettings.create().burnable().nonOpaque()}
    fun burnLightDestroy(): FabricBlockSettings { return FabricBlockSettings.create().burnable().nonOpaque().pistonBehavior(PistonBehavior.DESTROY)}
    fun burnDestroyMove(): FabricBlockSettings { return FabricBlockSettings.create().burnable().noCollision().pistonBehavior(PistonBehavior.DESTROY)}
    fun burnDestroy(): FabricBlockSettings { return FabricBlockSettings.create().burnable().pistonBehavior(PistonBehavior.DESTROY)}
    fun destroy(): FabricBlockSettings { return FabricBlockSettings.create().pistonBehavior(PistonBehavior.DESTROY)}
    fun lightDestroy(): FabricBlockSettings { return FabricBlockSettings.create().nonOpaque().pistonBehavior(PistonBehavior.DESTROY)}
    fun light(): FabricBlockSettings { return FabricBlockSettings.create().nonOpaque()}
    fun barrier(): FabricBlockSettings { return FabricBlockSettings.create().pistonBehavior(PistonBehavior.BLOCK)}
    fun nonSolidMove(): FabricBlockSettings { return FabricBlockSettings.create().collidable(false)}
}