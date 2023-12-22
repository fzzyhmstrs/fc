package me.fzzyhmstrs.fzzy_core.entity_util

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LightningEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.ToolMaterial
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

open class BasicCustomTridentEntity(entityType: EntityType<out BasicCustomTridentEntity?>?, world: World?) : TridentEntity(entityType,world) {
    private var dealtDamage = false
    private var damage = 8f

    constructor(entityType: EntityType<out BasicCustomTridentEntity?>?,world: World?, owner: LivingEntity?, stack: ItemStack) : this(
        entityType,
        world
    ) {
        setOwner(owner)
        this.tridentStack = stack.copy()
    }

    fun setDamage(damage: Float){
      this.damage = damage
    }
    fun setDamage(material:ToolMaterial) {
        setDamage(material.attackDamage)
    }

    private val isOwnerAlive: Boolean
        get() {
            val entity = owner
            return if (entity == null || !entity.isAlive) {
                false
            } else entity !is ServerPlayerEntity || !entity.isSpectator()
        }

    override fun getEntityCollision(currentPosition: Vec3d, nextPosition: Vec3d): EntityHitResult? {
        return if (dealtDamage) {
            null
        } else super.getEntityCollision(currentPosition, nextPosition)
    }

    open fun onOwnedHit(owner: LivingEntity, target: LivingEntity, source: DamageSource, amount: Float, stack: ItemStack): Float{
        return amount
    }

    open fun onOwnedKill(owner: LivingEntity, target: LivingEntity, stack: ItemStack) {
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        val blockPos: BlockPos?
        val entity = entityHitResult.entity
        var f = this.damage
        val livingEntity: Entity? = owner
        val trident = asItemStack()
        val damageSource = this.damageSources.trident(this, if (owner == null) this else livingEntity)
        if (entity is LivingEntity) {
            f += EnchantmentHelper.getAttackDamage(trident, entity.group)
            f = if (livingEntity is LivingEntity) {
                onOwnedHit(livingEntity, entity, damageSource, f, trident)
            } else {
                f
            }
        }
        
        dealtDamage = true
        var soundEvent = SoundEvents.ITEM_TRIDENT_HIT
        if (entity.damage(damageSource, f)) {
            if (entity.type === EntityType.ENDERMAN) {
                return
            }
            if (entity is LivingEntity) {
                if (livingEntity is LivingEntity) {
                    EnchantmentHelper.onUserDamaged(entity, livingEntity)
                    EnchantmentHelper.onTargetDamaged(livingEntity, entity)
                    if (entity.isDead)
                        onOwnedKill(livingEntity,entity, trident)
                }
                onHit(entity)
            }
        }
        velocity = velocity.multiply(-0.01, -0.1, -0.01)
        var volume = 1.0f
        if (world is ServerWorld && world.isThundering && hasChanneling() && world.isSkyVisible(entity.blockPos)) {
            blockPos = entity.blockPos
            val le = LightningEntity(EntityType.LIGHTNING_BOLT,world)
            //val lightningEntity = EntityType.LIGHTNING_BOLT.create(world)
            le.refreshPositionAfterTeleport(Vec3d.ofBottomCenter(blockPos))
            le.channeler =
                if (livingEntity is ServerPlayerEntity) livingEntity else null
            world.spawnEntity(le)
            soundEvent = SoundEvents.ITEM_TRIDENT_THUNDER
            volume = 5.0f
        }
        playSound(soundEvent, volume, 1.0f)
    }

    private fun hasChanneling(): Boolean {
        return EnchantmentHelper.hasChanneling(asItemStack())
    }

    override fun tryPickup(player: PlayerEntity): Boolean {
        return when (this.pickupType){
            PickupPermission.ALLOWED -> {
                if(isOffhand)
                    if (insertOffhand(asItemStack(),player))
                        true
                    else
                        player.inventory.insertStack(asItemStack())
                else
                    player.inventory.insertStack(asItemStack())
            }
            PickupPermission.CREATIVE_ONLY -> player.abilities.creativeMode
            else -> {
                this.isNoClip && isOwner(player) &&
                        if(isOffhand)
                            if (insertOffhand(asItemStack(),player))
                                true
                            else
                                player.inventory.insertStack(asItemStack())
                        else
                            player.inventory.insertStack(asItemStack())
            }
        }
    }

    private fun insertOffhand(stack: ItemStack, player: PlayerEntity): Boolean{
        if (stack.isEmpty) {
            return false
        }
        if (!player.inventory.offHand[0].isEmpty)
            return false
        player.inventory.offHand[0] = stack.copyAndEmpty()
        player.inventory.offHand[0].bobbingAnimationTime = 5
        return true
    }

    override fun getHitSound(): SoundEvent {
        return SoundEvents.ITEM_TRIDENT_HIT_GROUND
    }

    override fun onPlayerCollision(player: PlayerEntity) {
        if (isOwner(player) || owner == null) {
            super.onPlayerCollision(player)
        }
    }
}
