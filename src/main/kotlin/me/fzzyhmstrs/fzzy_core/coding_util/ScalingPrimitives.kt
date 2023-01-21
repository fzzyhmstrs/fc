package me.fzzyhmstrs.fzzy_core.coding_util

/**
 * simple data classes that store various primitives as well as scaling factors, and return the result of scaling the base value by the defined number of scaling levels.
 *
 * Example: A [PerLvlI] with a base of 10 and a perLevel of 1 will output 14 with a level input of 4 and output 8 with a level of -2
 *
 * these classes can be added together with the plus method.
 */

data class PerLvlI(val base: Int = 0, val perLevel: Int = 0, val percent: Int = 0){
    fun value(level: Int): Int{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldi: PerLvlI): PerLvlI {
        return PerLvlI(base + ldi.base, perLevel + ldi.perLevel, percent + ldi.percent)
    }
}

data class PerLvlL(val base: Long = 0, val perLevel: Long = 0, val percent: Long = 0){
    fun value(level: Long): Long{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldl: PerLvlL): PerLvlL {
        return PerLvlL(base + ldl.base, perLevel + ldl.perLevel, percent + ldl.percent)
    }
}

data class PerLvlF(val base: Float = 0.0F, val perLevel: Float = 0.0F, val percent: Float = 0.0F){
    fun value(level: Int): Float{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldf: PerLvlF): PerLvlF {
        return PerLvlF(base + ldf.base, perLevel + ldf.perLevel, percent + ldf.percent)
    }
}

data class PerLvlD(val base: Double = 0.0, val perLevel: Double = 0.0, val percent: Double = 0.0){
    fun value(level: Int): Double{
        return (base + perLevel * level) * (100 + percent) / 100
    }
    fun plus(ldd: PerLvlD): PerLvlD {
        return PerLvlD(base + ldd.base, perLevel + ldd.perLevel, percent + ldd.percent)
    }
}
