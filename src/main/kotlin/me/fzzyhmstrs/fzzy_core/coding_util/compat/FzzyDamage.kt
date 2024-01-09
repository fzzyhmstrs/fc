package me.fzzyhmstrs.fzzy_core.coding_util.compat

object FzzyDamage{

    fun generic(origin: Entity): DamageSource {
        return origin.damageSources.generic()
    }
    
    fun magic(origin: Entity): DamageSource {
        return origin.damageSources.magic()
    }
    
    fun indirectMagic(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.indirectMagic(source, attacker)
    }
    
    fun mobAttack(origin: Entity, source: Entity? = origin): DamageSource{
        return origin.damageSources.mobAttack(source)
    }

    fun mobProjectile(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.mobProjectile(source, attacker)
    }

    fun playerAttack(origin: Entity, source: PlayerEntity? = origin as? PlayerEntity): DamageSource{
        return origin.damageSources.playerAttack(source)
    }

    fun sonicBoom(origin: Entity, source: Entity? = origin): DamageSource{
        return origin.damageSources.sonicBoom(source)
    }

    fun fireball(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.fireball(source, attacker)
    }

    fun explosion(origin: Entity, source: Entity? = origin, attacker: Entity? = origin): DamageSource{
        return origin.damageSources.explosion(source, attacker)
    }

    fun thorns(origin: Entity, source: Entity? = origin): DamageSource{
        return origin.damageSources.thorns(source)
    }

    fun freeze(origin: Entity): DamageSource {
        return origin.damageSources.freeze()
    }

    fun lightning(origin: Entity): DamageSource {
        return origin.damageSources.lightningBolt()
    }
    
}
