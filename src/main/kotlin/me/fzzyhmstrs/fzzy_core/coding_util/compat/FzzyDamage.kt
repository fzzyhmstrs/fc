package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.projectile.AbstractFireballEntity
import net.minecraft.entity.projectile.FireworkRocketEntity
import net.minecraft.entity.projectile.PersistentProjectileEntity
import net.minecraft.entity.projectile.WitherSkullEntity
import net.minecraft.world.explosion.Explosion

object FzzyDamage{

    fun inFire(origin: Entity): DamageSource {
        return DamageSource.IN_FIRE
    }

    fun lightning(origin: Entity): DamageSource {
        return DamageSource.LIGHTNING_BOLT
    }

    fun onFire(origin: Entity): DamageSource {
        return DamageSource.ON_FIRE
    }

    fun lava(origin: Entity): DamageSource {
        return DamageSource.LAVA
    }

    fun hotFloor(origin: Entity): DamageSource {
        return DamageSource.HOT_FLOOR
    }

    fun inWall(origin: Entity): DamageSource {
        return DamageSource.IN_WALL
    }

    fun cramming(origin: Entity): DamageSource {
        return DamageSource.CRAMMING
    }

    fun drown(origin: Entity): DamageSource {
        return DamageSource.DROWN
    }

    fun starve(origin: Entity): DamageSource {
        return DamageSource.STARVE
    }

    fun cactus(origin: Entity): DamageSource {
        return DamageSource.CACTUS
    }

    fun fall(origin: Entity): DamageSource {
        return DamageSource.FALL
    }

    fun flyIntoWall(origin: Entity): DamageSource {
        return DamageSource.FLY_INTO_WALL
    }

    fun outOfWorld(origin: Entity): DamageSource {
        return DamageSource.OUT_OF_WORLD
    }

    fun generic(origin: Entity): DamageSource {
        return DamageSource.GENERIC
    }
    
    fun magic(origin: Entity): DamageSource {
        return DamageSource.MAGIC
    }

    fun wither(origin: Entity): DamageSource {
        return DamageSource.WITHER
    }

    fun dragonBreath(origin: Entity): DamageSource {
        return DamageSource.DRAGON_BREATH
    }

    fun dryOut(origin: Entity): DamageSource {
        return DamageSource.DRYOUT
    }

    fun sweetBerry(origin: Entity): DamageSource {
        return DamageSource.SWEET_BERRY_BUSH
    }

    fun freeze(origin: Entity): DamageSource {
        return DamageSource.FREEZE
    }

    fun stalagmite(origin: Entity): DamageSource {
        return DamageSource.STALAGMITE
    }

    fun sting(origin: Entity, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return DamageSource.sting(attacker)
    }

    fun mobAttack(origin: Entity, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return DamageSource.mob(attacker)
    }

    fun mobProjectile(origin: Entity, source: Entity? = origin, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return DamageSource.mobProjectile(source,attacker)
    }

    fun playerAttack(origin: Entity, attacker: PlayerEntity? = origin as? PlayerEntity): DamageSource{
        return DamageSource.player(attacker)
    }

    fun arrow(origin: Entity, source: PersistentProjectileEntity? = origin as? PersistentProjectileEntity, attacker: Entity? = origin): DamageSource{
        return DamageSource.arrow(source,attacker)
    }

    fun trident(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return DamageSource.trident(source,attacker)
    }

    fun firework(origin: Entity, source: FireworkRocketEntity? = origin as? FireworkRocketEntity, attacker: Entity? = origin): DamageSource{
        return DamageSource.firework(source,attacker)
    }

    fun fireball(origin: Entity, source: AbstractFireballEntity? = origin as? AbstractFireballEntity, attacker: Entity? = origin): DamageSource{
        return DamageSource.fireball(source,attacker)
    }

    fun witherSkull(origin: Entity, source: WitherSkullEntity? = origin as? WitherSkullEntity, attacker: Entity? = origin): DamageSource{
        return DamageSource.witherSkull(source,attacker)
    }

    fun thrown(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return DamageSource.thrownProjectile(source,attacker)
    }

    fun indirectMagic(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return DamageSource.magic(source,attacker)
    }

    fun thorns(origin: Entity, source: Entity? = origin): DamageSource{
        return DamageSource.thorns(source)
    }

    fun explosion(origin: Entity, explosion: Explosion?): DamageSource{
        return DamageSource.explosion(explosion)
    }

    fun explosion(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return DamageSource.explosion(attacker as? LivingEntity)
    }

    fun sonicBoom(origin: Entity, attacker: Entity? = origin): DamageSource{
        return DamageSource.sonicBoom(attacker)
    }
}
