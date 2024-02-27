package me.fzzyhmstrs.fzzy_core.modifier_util.serialization

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.entity.effect.StatusEffectInstance
import java.util.Optional
import java.util.UUID

object CustomCodecs {

    val statusEffectInstanceCodec: Codec<StatusEffectInstance> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<StatusEffectInstance> ->
        instance.group(
            FzzyPort.STATUS_EFFECT.registry().codec.fieldOf("type").forGetter { i -> i.effectType },
            Codec.INT.optionalFieldOf("duration",0).forGetter { i -> i.duration },
            Codec.INT.optionalFieldOf("amplifier",0).forGetter { i -> i.amplifier },
            Codec.BOOL.optionalFieldOf("ambient",false).forGetter { i -> i.isAmbient },
            Codec.BOOL.optionalFieldOf("show_particles",true).forGetter { i -> i.shouldShowParticles() },
            Codec.BOOL.optionalFieldOf("show_icon",false).forGetter { i -> i.shouldShowIcon() }
        ).apply(instance){t,d,a,m,p,i -> StatusEffectInstance(t,d,a,m,p,i) }
    }

    val uuidCodec = Codec.STRING.xmap(
        {s -> UUID.fromString(s)},
        {u -> u.toString()}
    )

    val operationCodec = Codec.either(
        Codec.intRange(0,2),
        Codec.STRING
    ).comapFlatMap(
        {e -> try{ DataResult.success(e.map({l -> Operation.fromId(l)}, {r -> Operation.valueOf(r)}))} catch (e: Exception){ DataResult.error { "Operation Enum not valid: $e. Needs to be either 0, 1, 2, or ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL" }} },
        {o -> Either.left(o.id)}
    )

    val entityAttributeModifierCodec: Codec<EntityAttributeModifier> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<EntityAttributeModifier> ->
        instance.group(
            uuidCodec.optionalFieldOf("uuid").forGetter { e -> Optional.of(e.id) },
            Codec.STRING.fieldOf("name").forGetter { e -> e.name },
            Codec.DOUBLE.fieldOf("value").forGetter { e -> e.value },
            operationCodec.fieldOf("operation").forGetter { e -> e.operation }
        ).apply(instance){u,n,v,o -> if (u.isPresent) EntityAttributeModifier(u.get(),n,v,o) else EntityAttributeModifier(n,v,o)}
    }

}