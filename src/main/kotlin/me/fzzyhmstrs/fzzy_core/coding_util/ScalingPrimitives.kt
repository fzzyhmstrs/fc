package me.fzzyhmstrs.fzzy_core.coding_util

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.nbt.NbtCompound

/**
 * simple data classes that store various primitives as well as scaling factors, and return the result of scaling the base value by the defined number of scaling levels.
 *
 * Example: A [PerLvlI] with a base of 10 and a perLevel of 1 will output 14 with a level input of 4 and output 8 with a level of -2
 *
 * these classes can be added together with the plus method.
 */

object ScalingPrimitivesHelper {

    fun writeNbt(primitive: ScalingPrimitives<*>): NbtCompound{
        val nbtCompound = NbtCompound()
        when (primitive){
            is PerLvlI ->{
                nbtCompound.putString("type","i")
                nbtCompound.putInt("base",primitive.base())
                nbtCompound.putInt("perLevel",primitive.perLevel())
                nbtCompound.putInt("percent",primitive.percent())
            }
            is PerLvlF ->{
                nbtCompound.putString("type","f")
                nbtCompound.putFloat("base",primitive.base())
                nbtCompound.putFloat("perLevel",primitive.perLevel())
                nbtCompound.putFloat("percent",primitive.percent())
            }
            is PerLvlD ->{
                nbtCompound.putString("type","d")
                nbtCompound.putDouble("base",primitive.base())
                nbtCompound.putDouble("perLevel",primitive.perLevel())
                nbtCompound.putDouble("percent",primitive.percent())
            }
            is PerLvlL ->{
                nbtCompound.putString("type","l")
                nbtCompound.putLong("base",primitive.base())
                nbtCompound.putLong("perLevel",primitive.perLevel())
                nbtCompound.putLong("percent",primitive.percent())
            }
        }
        return nbtCompound
    }

    fun readNbt(nbtCompound: NbtCompound): ScalingPrimitives<*>{
        when(nbtCompound.getString("type")){
            "i" -> {
                return PerLvlI(
                    nbtCompound.getInt("base"),
                    nbtCompound.getInt("perLevel"),
                    nbtCompound.getInt("percent")
                )
            }
            "f" -> {
                return PerLvlF(
                    nbtCompound.getFloat("base"),
                    nbtCompound.getFloat("perLevel"),
                    nbtCompound.getFloat("percent")
                )
            }
            "d" -> {
                return PerLvlD(
                    nbtCompound.getDouble("base"),
                    nbtCompound.getDouble("perLevel"),
                    nbtCompound.getDouble("percent")
                )
            }
            "l" -> {
                return PerLvlL(
                    nbtCompound.getLong("base"),
                    nbtCompound.getLong("perLevel"),
                    nbtCompound.getLong("percent")
                )
            }
        }
        throw IllegalStateException("Scaling Primitive couldn't be read from NbtCompound: $nbtCompound")
    }

}

interface ScalingPrimitives<T: Number>{
}

data class PerLvlI(private var base: Int = 0, private var perLevel: Int = 0, private var percent: Int = 0): ScalingPrimitives<Int>{
    fun base(): Int{
        return base
    }
     fun perLevel(): Int{
        return perLevel
    }
     fun percent(): Int{
        return percent
    }
     fun value(level: Int): Int{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldi: PerLvlI): PerLvlI {
        this.base += ldi.base
        this.perLevel += ldi.perLevel
        this.percent += ldi.percent
        return this
    }
    fun plus(base: Int = 0, perLevel: Int = 0, percent: Int = 0): PerLvlI {
        this.base += base
        this.perLevel += perLevel
        this.percent += percent
        return this
    }
    fun set(base: Int = 0, perLevel: Int = 0, percent: Int = 0): PerLvlI{
        this.base = base
        this.perLevel = perLevel
        this.percent = percent
        return this
    }

    companion object{
        val CODEC: Codec<PerLvlI> = Codec.either(
            Codec.INT,
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PerLvlI> ->
                instance.group(
                    Codec.INT.optionalFieldOf("base", 0).forGetter { perLvlI -> perLvlI.base() },
                    Codec.INT.optionalFieldOf("perLevel", 0).forGetter { perLvlI -> perLvlI.perLevel() },
                    Codec.INT.optionalFieldOf("percent", 0).forGetter { perLvlI -> perLvlI.percent() }
                ).apply(instance){base, perLevel, percent -> PerLvlI(base, perLevel, percent)} }
        ).xmap(
            {either -> either.map({i -> PerLvlI(i)},{pli -> pli})},
            {pli -> if(pli.perLevel() == 0 && pli.percent() == 0) Either.left(pli.base()) else Either.right(pli)}
        )
    }
}

data class PerLvlL(private var base: Long = 0, private var perLevel: Long = 0, private var percent: Long = 0): ScalingPrimitives<Long>{
    fun base(): Long{
        return base
    }
    fun perLevel(): Long{
        return perLevel
    }
    fun percent(): Long{
        return percent
    }
     fun value(level: Int): Long{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldl: PerLvlL): PerLvlL {
        this.base += ldl.base
        this.perLevel += ldl.perLevel
        this.percent += ldl.percent
        return this
    }
    fun plus(base: Long = 0, perLevel: Long = 0, percent: Long = 0): PerLvlL {
        this.base += base
        this.perLevel += perLevel
        this.percent += percent
        return this
    }
    fun set(base: Long = 0, perLevel: Long = 0, percent: Long = 0): PerLvlL{
        this.base = base
        this.perLevel = perLevel
        this.percent = percent
        return this
    }

    companion object {
        val CODEC: Codec<PerLvlL> = Codec.either(
            Codec.LONG,
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PerLvlL> ->
                instance.group(
                    Codec.LONG.optionalFieldOf("base", 0L).forGetter { perLvlL -> perLvlL.base() },
                    Codec.LONG.optionalFieldOf("perLevel", 0L).forGetter { perLvlL -> perLvlL.perLevel() },
                    Codec.LONG.optionalFieldOf("percent", 0L).forGetter { perLvlL -> perLvlL.percent() }
                ).apply(instance) { base, perLevel, percent -> PerLvlL(base, perLevel, percent) }
            }
        ).xmap(
            { either -> either.map({ l -> PerLvlL(l) }, { pll -> pll }) },
            { pll -> if (pll.perLevel() == 0L && pll.percent() == 0L) Either.left(pll.base()) else Either.right(pll) }
        )
    }
}

data class PerLvlF(private var base: Float = 0.0F, private var perLevel: Float = 0.0F, private var percent: Float = 0.0F): ScalingPrimitives<Float>{
     fun base(): Float{
        return base
    }
     fun perLevel(): Float{
        return perLevel
    }
     fun percent(): Float{
        return percent
    }
     fun value(level: Int): Float{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldf: PerLvlF): PerLvlF {
        this.base += ldf.base
        this.perLevel += ldf.perLevel
        this.percent += ldf.percent
        return this
    }
    fun plus(base: Float = 0f, perLevel: Float = 0f, percent: Float = 0f): PerLvlF {
        this.base += base
        this.perLevel += perLevel
        this.percent += percent
        return this
    }
    fun set(base: Float = 0f, perLevel: Float = 0f, percent: Float = 0f): PerLvlF{
        this.base = base
        this.perLevel = perLevel
        this.percent = percent
        return this
    }

    companion object {
        val CODEC: Codec<PerLvlF> = Codec.either(
            Codec.FLOAT,
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PerLvlF> ->
                instance.group(
                    Codec.FLOAT.optionalFieldOf("base", 0f).forGetter { perLvlF -> perLvlF.base() },
                    Codec.FLOAT.optionalFieldOf("perLevel", 0f).forGetter { perLvlF -> perLvlF.perLevel() },
                    Codec.FLOAT.optionalFieldOf("percent", 0f).forGetter { perLvlF -> perLvlF.percent() }
                ).apply(instance){base, perLevel, percent -> PerLvlF(base, perLevel, percent)} }
        ).xmap(
            { either -> either.map({ f -> PerLvlF(f) }, { plf -> plf }) },
            { plf -> if (plf.perLevel() == 0f && plf.percent() == 0f) Either.left(plf.base()) else Either.right(plf) }
        )
    }
}

data class PerLvlD(private var base: Double = 0.0, private var perLevel: Double = 0.0, private var percent: Double = 0.0): ScalingPrimitives<Double>{
     fun base(): Double{
        return base
    }
     fun perLevel(): Double{
        return perLevel
    }
     fun percent(): Double{
        return percent
    }
     fun value(level: Int): Double{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldd: PerLvlD): PerLvlD {
        this.base += ldd.base
        this.perLevel += ldd.perLevel
        this.percent += ldd.percent
        return this
    }
    fun plus(base: Double = 0.0, perLevel: Double = 0.0, percent: Double = 0.0): PerLvlD {
        this.base += base
        this.perLevel += perLevel
        this.percent += percent
        return this
    }
    fun set(base: Double = 0.0, perLevel: Double = 0.0, percent: Double = 0.0): PerLvlD{
        this.base = base
        this.perLevel = perLevel
        this.percent = percent
        return this
    }

    companion object{
        val CODEC: Codec<PerLvlD> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PerLvlD> ->
            instance.group(
                Codec.DOUBLE.optionalFieldOf("base", 0.0).forGetter { perLvlD -> perLvlD.base() },
                Codec.DOUBLE.optionalFieldOf("perLevel", 0.0).forGetter { perLvlD -> perLvlD.perLevel() },
                Codec.DOUBLE.optionalFieldOf("percent", 0.0).forGetter { perLvlD -> perLvlD.percent() }
            ).apply(instance){base, perLevel, percent -> PerLvlD(base, perLevel, percent)} }

        val coed = Codec.either(
            Codec.DOUBLE,
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<PerLvlD> ->
                instance.group(
                    Codec.DOUBLE.optionalFieldOf("base", 0.0).forGetter { perLvlD -> perLvlD.base() },
                    Codec.DOUBLE.optionalFieldOf("perLevel", 0.0).forGetter { perLvlD -> perLvlD.perLevel() },
                    Codec.DOUBLE.optionalFieldOf("percent", 0.0).forGetter { perLvlD -> perLvlD.percent() }
                ).apply(instance){base, perLevel, percent -> PerLvlD(base, perLevel, percent)} }
        ).xmap(
            { either -> either.map({ d -> PerLvlD(d) }, { pld -> pld }) },
            { pld -> if (pld.perLevel() == 0.0 && pld.percent() == 0.0) Either.left(pld.base()) else Either.right(pld) }
        )
    }
}