package me.fzzyhmstrs.fzzy_core.entity_util

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder.Factory
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup

open class EntityBuilder {
    fun <T: Entity> buildEntity(group: SpawnGroup, factory: EntityType.EntityFactory<T>, w: Float, h: Float, trackedRange: Int = 5, trackedUpdateRate: Int = 3): EntityType<T> {
        return FabricEntityTypeBuilder.create(group, factory).dimensions(EntityDimensions.fixed(w,h)).trackRangeChunks(trackedRange).trackedUpdateRate(trackedUpdateRate).build()
    }
    fun <T: Entity> buildCreature(factory: EntityType.EntityFactory<T>, w: Float, h: Float, trackedRange: Int = 5, trackedUpdateRate: Int = 3): EntityType<T> {
        return buildEntity(SpawnGroup.CREATURE,factory,w, h, trackedRange, trackedUpdateRate)
    }
    fun <T: Entity> buildMisc(factory: EntityType.EntityFactory<T>, w: Float, h: Float, trackedRange: Int = 5, trackedUpdateRate: Int = 3): EntityType<T> {
        return buildEntity(SpawnGroup.MISC,factory,w, h, trackedRange, trackedUpdateRate)
    }
    fun <T: Entity> buildMonster(factory: EntityType.EntityFactory<T>, w: Float, h: Float, trackedRange: Int = 5, trackedUpdateRate: Int = 3): EntityType<T> {
        return buildEntity(SpawnGroup.MONSTER,factory,w, h, trackedRange, trackedUpdateRate)
    }

    fun <T: BlockEntity> buildBlockEntity(factory: Factory<T>, vararg block: Block): BlockEntityType<T>{
        return FabricBlockEntityTypeBuilder.create(factory, *block).build()
    }
}