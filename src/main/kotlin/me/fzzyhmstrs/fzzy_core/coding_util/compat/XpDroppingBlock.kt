package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.block.OreBlock
import net.minecraft.util.math.intprovider.ConstantIntProvider
import net.minecraft.util.math.intprovider.IntProvider

open class XpDroppingBlock(settings: Settings, experience: IntProvider): OreBlock(settings, experience) {
    constructor(settings: Settings): this(settings,ConstantIntProvider.create(0))
}
