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
        return origin.damageSources.inFire()
    }

    fun lightning(origin: Entity): DamageSource {
        return origin.damageSources.lightningBolt()
    }

    fun onFire(origin: Entity): DamageSource {
        return origin.damageSources.onFire()
    }

    fun lava(origin: Entity): DamageSource {
        return origin.damageSources.lava()
    }

    fun hotFloor(origin: Entity): DamageSource {
        return origin.damageSources.hotFloor()
    }

    fun inWall(origin: Entity): DamageSource {
        return origin.damageSources.inWall()
    }

    fun cramming(origin: Entity): DamageSource {
        return origin.damageSources.cramming()
    }

    fun drown(origin: Entity): DamageSource {
        return origin.damageSources.drown()
    }

    fun starve(origin: Entity): DamageSource {
        return origin.damageSources.starve()
    }

    fun cactus(origin: Entity): DamageSource {
        return origin.damageSources.cactus()
    }

    fun fall(origin: Entity): DamageSource {
        return origin.damageSources.fall()
    }

    fun flyIntoWall(origin: Entity): DamageSource {
        return origin.damageSources.flyIntoWall()
    }

    fun outOfWorld(origin: Entity): DamageSource {
        return origin.damageSources.outOfWorld()
    }

    fun generic(origin: Entity): DamageSource {
        return origin.damageSources.generic()
    }
    
    fun magic(origin: Entity): DamageSource {
        return origin.damageSources.magic()
    }

    fun wither(origin: Entity): DamageSource {
        return origin.damageSources.wither()
    }

    fun dragonBreath(origin: Entity): DamageSource {
        return origin.damageSources.dragonBreath()
    }

    fun dryOut(origin: Entity): DamageSource {
        return origin.damageSources.dryOut()
    }

    fun sweetBerry(origin: Entity): DamageSource {
        return origin.damageSources.sweetBerryBush()
    }

    fun freeze(origin: Entity): DamageSource {
        return origin.damageSources.freeze()
    }

    fun stalagmite(origin: Entity): DamageSource {
        return origin.damageSources.stalagmite()
    }

    fun sting(origin: Entity, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return origin.damageSources.sting(attacker)
    }

    fun mobAttack(origin: Entity, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return origin.damageSources.mobAttack(attacker)
    }

    fun mobProjectile(origin: Entity, source: Entity? = origin, attacker: LivingEntity? = origin as? LivingEntity): DamageSource{
        return origin.damageSources.mobProjectile(source, attacker)
    }

    fun playerAttack(origin: Entity, attacker: PlayerEntity? = origin as? PlayerEntity): DamageSource{
        return origin.damageSources.playerAttack(attacker)
    }

    fun arrow(origin: Entity, source: PersistentProjectileEntity? = origin as? PersistentProjectileEntity, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.arrow(source, attacker)
    }

    fun trident(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.trident(source, attacker)
    }

    fun firework(origin: Entity, source: FireworkRocketEntity? = origin as? FireworkRocketEntity, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.fireworks(source, attacker)
    }

    fun fireball(origin: Entity, source: AbstractFireballEntity? = origin as? AbstractFireballEntity, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.fireball(source, attacker)
    }

    fun witherSkull(origin: Entity, source: WitherSkullEntity? = origin as? WitherSkullEntity, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.witherSkull(source, attacker)
    }

    fun thrown(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.thrown(source, attacker)
    }

    fun indirectMagic(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.indirectMagic(source, attacker)
    }

    fun thorns(origin: Entity, source: Entity? = origin): DamageSource{
        return origin.damageSources.thorns(source)
    }

    fun explosion(origin: Entity, explosion: Explosion?): DamageSource{
        return origin.damageSources.explosion(explosion)
    }

    fun explosion(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.explosion(source, attacker)
    }

    fun sonicBoom(origin: Entity, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.sonicBoom(attacker)
    }
}
