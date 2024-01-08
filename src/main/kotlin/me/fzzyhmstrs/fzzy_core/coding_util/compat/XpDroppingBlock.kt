package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.block.ExperienceDroppingBlock
import net.minecraft.util.math.intprovider.ConstantIntProvider
import net.minecraft.util.math.intprovider.IntProvider

class XpDroppingBlock(settings: Settings, experience: IntProvider): ExperienceDroppingBlock(settings, experience) {
    constructor(settings: Settings): this(settings,ConstantIntProvider.create(0))
}