package me.fzzyhmstrs.fzzy_core.coding_util.compat

import net.minecraft.util.math.MathHelper
import org.joml.Math
import org.joml.Quaternionf

enum class FzzyRotation {
    NEGATIVE_X{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin((-r) * 0.5f)
            val cos = Math.cosFromSin(sin, (-r) * 0.5f)
            return Quaternionf(sin, 0f,0f, cos)
        }
    },
    POSITIVE_X{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin(r * 0.5f)
            val cos = Math.cosFromSin(sin, r * 0.5f)
            return Quaternionf(sin, 0f,0f, cos)
        }
    },
    NEGATIVE_Y{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin((-r) * 0.5f)
            val cos = Math.cosFromSin(sin, (-r) * 0.5f)
            return Quaternionf(0f, sin,0f, cos)
        }
    },
    POSITIVE_Y{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin(r * 0.5f)
            val cos = Math.cosFromSin(sin, r * 0.5f)
            return Quaternionf(0f, sin,0f, cos)
        }
    },
    NEGATIVE_Z{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin((-r) * 0.5f)
            val cos = Math.cosFromSin(sin, (-r) * 0.5f)
            return Quaternionf(0f, 0f, sin, cos)
        }
    },
    POSITIVE_Z{
        override fun rotation(r: Float): Quaternionf {
            val sin = Math.sin(r * 0.5f)
            val cos = Math.cosFromSin(sin, r * 0.5f)
            return Quaternionf(0f, 0f, sin, cos)
        }
    }
    ;
    abstract fun rotation(r: Float): Quaternionf
    fun degrees(d: Float): Quaternionf{
        return rotation(d * MathHelper.RADIANS_PER_DEGREE)
    }
}