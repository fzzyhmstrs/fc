package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Quaternion
import kotlin.math.sin
import kotlin.math.sqrt

@Suppress("unused")
enum class FzzyRotation {
    NEGATIVE_X{
        override fun rotation(r: Float): Quaternion {
            val sin = sin((-r) * 0.5f)
            val cos = cosFromSinInternal(sin, (-r) * 0.5f)
            return Quaternion(sin, 0f,0f, cos)
        }
    },
    POSITIVE_X{
        override fun rotation(r: Float): Quaternion {
            val sin = sin(r * 0.5f)
            val cos = cosFromSinInternal(sin, r * 0.5f)
            return Quaternion(sin, 0f,0f, cos)
        }
    },
    NEGATIVE_Y{
        override fun rotation(r: Float): Quaternion {
            val sin = sin((-r) * 0.5f)
            val cos = cosFromSinInternal(sin, (-r) * 0.5f)
            return Quaternion(0f, sin,0f, cos)
        }
    },
    POSITIVE_Y{
        override fun rotation(r: Float): Quaternion {
            val sin = sin(r * 0.5f)
            val cos = cosFromSinInternal(sin, r * 0.5f)
            return Quaternion(0f, sin,0f, cos)
        }
    },
    NEGATIVE_Z{
        override fun rotation(r: Float): Quaternion {
            val sin = sin((-r) * 0.5f)
            val cos = cosFromSinInternal(sin, (-r) * 0.5f)
            return Quaternion(0f, 0f, sin, cos)
        }
    },
    POSITIVE_Z{
        override fun rotation(r: Float): Quaternion {
            val sin = sin(r * 0.5f)
            val cos = cosFromSinInternal(sin, r * 0.5f)
            return Quaternion(0f, 0f, sin, cos)
        }
    }
    ;
    abstract fun rotation(r: Float): Quaternion
    fun degrees(d: Float): Quaternion{
        return rotation(d * MathHelper.RADIANS_PER_DEGREE)
    }
    fun rotationDegrees(d: Float): Quaternion{
        return rotation(d * MathHelper.RADIANS_PER_DEGREE)
    }
    fun rotationRadians(r: Float): Quaternion{
        return rotation(r)
    }
    fun radians(r: Float): Quaternion{
        return rotation(r)
    }
    protected fun cosFromSinInternal(sin: Float, angle: Float): Float {
        // sin(x)^2 + cos(x)^2 = 1
        val cos = sqrt((1.0f - sin * sin).toDouble()).toFloat()
        val a: Float = angle + piHalfF
        var b: Float = a - (a / pi2F).toInt() * pi2F
        if (b < 0.0) b += pi2F
        return if (b >= piF) -cos else cos
    }

    private val piF = Math.PI.toFloat()
    private val pi2F = piF * 2.0f
    private val piHalfF = (Math.PI * 0.5).toFloat()
}