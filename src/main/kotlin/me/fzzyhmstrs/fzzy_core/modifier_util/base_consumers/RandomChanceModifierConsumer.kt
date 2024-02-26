package me.fzzyhmstrs.fzzy_core.modifier_util.base_consumers

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumer
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierConsumerType
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.random.Random

class RandomChanceModifierConsumer(private val chance: Float, private val child: ModifierConsumer): ModifierConsumer {
    override fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?) {
        if (random.nextFloat() < chance){
            child.apply(stack, user, target)
        }
    }

    override fun getType(): ModifierConsumerType<*> {
        return Type
    }

    companion object Type: ModifierConsumerType<RandomChanceModifierConsumer>{

        private val random = Random.createThreadSafe()

        private val codec = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<RandomChanceModifierConsumer> ->
            instance.group(
                Codec.FLOAT.fieldOf("chance").forGetter { c -> c.chance },
                ModifierConsumerType.CODEC.fieldOf("child").forGetter { c -> c.child }
            ).apply(instance){f,c -> RandomChanceModifierConsumer(f,c)}
        }

        override fun codec(): Codec<RandomChanceModifierConsumer> {
            return codec
        }

    }
}