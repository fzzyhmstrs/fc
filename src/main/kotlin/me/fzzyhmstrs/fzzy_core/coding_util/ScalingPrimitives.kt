package me.fzzyhmstrs.fzzy_core.coding_util

import net.minecraft.nbt.NbtCompound

/**
 * simple data classes that store various primitives as well as scaling factors, and return the result of scaling the base value by the defined number of scaling levels.
 *
 * Example: A [PerLvlI] with a base of 10 and a perLevel of 1 will output 14 with a level input of 4 and output 8 with a level of -2
 *
 * these classes can be added together with the plus method.
 */

object ScalingPrimitivesHelper {
    fun writeNbt(primitive: ScalingPrimitives): NbtCompound{
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

    fun readNbt(nbtCompound: NbtCompound): ScalingPrimitives{
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

interface ScalingPrimitives

data class PerLvlI(private var base: Int = 0, private var perLevel: Int = 0, private var percent: Int = 0): ScalingPrimitives{
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
}

data class PerLvlL(private var base: Long = 0, private var perLevel: Long = 0, private var percent: Long = 0): ScalingPrimitives{
    fun base(): Long{
        return base
    }
    fun perLevel(): Long{
        return perLevel
    }
    fun percent(): Long{
        return percent
    }
    fun value(level: Long): Long{
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
}

data class PerLvlF(private var base: Float = 0.0F, private var perLevel: Float = 0.0F, private var percent: Float = 0.0F): ScalingPrimitives{
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
}

data class PerLvlD(private var base: Double = 0.0, private var perLevel: Double = 0.0, private var percent: Double = 0.0): ScalingPrimitives{
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
}
