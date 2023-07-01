package me.fzzyhmstrs.fzzy_core.coding_util

import net.minecraft.nbt.NbtCompound
import java.lang.IllegalStateException

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
                nbtCompound.putInt("base",primitive.base)
                nbtCompound.putInt("perLevel",primitive.perLevel)
                nbtCompound.putInt("percent",primitive.percent)
            }
            is PerLvlF ->{
                nbtCompound.putString("type","f")
                nbtCompound.putFloat("base",primitive.base)
                nbtCompound.putFloat("perLevel",primitive.perLevel)
                nbtCompound.putFloat("percent",primitive.percent)
            }
            is PerLvlD ->{
                nbtCompound.putString("type","d")
                nbtCompound.putDouble("base",primitive.base)
                nbtCompound.putDouble("perLevel",primitive.perLevel)
                nbtCompound.putDouble("percent",primitive.percent)
            }
            is PerLvlL ->{
                nbtCompound.putString("type","l")
                nbtCompound.putLong("base",primitive.base)
                nbtCompound.putLong("perLevel",primitive.perLevel)
                nbtCompound.putLong("percent",primitive.percent)
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

data class PerLvlI(val base: Int = 0, val perLevel: Int = 0, val percent: Int = 0): ScalingPrimitives{
    fun value(level: Int): Int{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldi: PerLvlI): PerLvlI {
        return PerLvlI(base + ldi.base, perLevel + ldi.perLevel, percent + ldi.percent)
    }
    fun plus(base: Int = 0, perLevel: Int = 0, percent: Int = 0): PerLvlI {
        return PerLvlI(this.base + base, this.perLevel + perLevel, this.percent + percent)
    }
}

data class PerLvlL(val base: Long = 0, val perLevel: Long = 0, val percent: Long = 0): ScalingPrimitives{
    fun value(level: Long): Long{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldl: PerLvlL): PerLvlL {
        return PerLvlL(base + ldl.base, perLevel + ldl.perLevel, percent + ldl.percent)
    }
    fun plus(base: Long = 0, perLevel: Long = 0, percent: Long = 0): PerLvlL {
        return PerLvlL(this.base + base, this.perLevel + perLevel, this.percent + percent)
    }
}

data class PerLvlF(val base: Float = 0.0F, val perLevel: Float = 0.0F, val percent: Float = 0.0F): ScalingPrimitives{
    fun value(level: Int): Float{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldf: PerLvlF): PerLvlF {
        return PerLvlF(base + ldf.base, perLevel + ldf.perLevel, percent + ldf.percent)
    }
    fun plus(base: Float = 0f, perLevel: Float = 0f, percent: Float = 0f): PerLvlF {
        return PerLvlF(this.base + base, this.perLevel + perLevel, this.percent + percent)
    }
}

data class PerLvlD(val base: Double = 0.0, val perLevel: Double = 0.0, val percent: Double = 0.0): ScalingPrimitives{
    fun value(level: Int): Double{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldd: PerLvlD): PerLvlD {
        return PerLvlD(base + ldd.base, perLevel + ldd.perLevel, percent + ldd.percent)
    }
    fun plus(base: Double = 0.0, perLevel: Double = 0.0, percent: Double = 0.0): PerLvlD {
        return PerLvlD(this.base + base, this.perLevel + perLevel, this.percent + percent)
    }
}
