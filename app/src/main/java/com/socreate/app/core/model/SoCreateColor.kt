package com.socreate.app.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a single color with full color management support.
 * Supports RGBA, HSB, and opacity for professional color workflows.
 */
@Serializable
data class SoCreateColor(
    val red: Float = 0f,
    val green: Float = 0f,
    val blue: Float = 0f,
    val alpha: Float = 1f,
    val colorSpace: ColorSpace = ColorSpace.SRGB
) {
    companion object {
        val BLACK = SoCreateColor(0f, 0f, 0f)
        val WHITE = SoCreateColor(1f, 1f, 1f)
        val TRANSPARENT = SoCreateColor(0f, 0f, 0f, 0f)
        val RED = SoCreateColor(1f, 0f, 0f)
        val GREEN = SoCreateColor(0f, 1f, 0f)
        val BLUE = SoCreateColor(0f, 0f, 1f)

        /** Create from HSV/HSB values */
        fun fromHSB(hue: Float, saturation: Float, brightness: Float, alpha: Float = 1f): SoCreateColor {
            val c = brightness * saturation
            val x = c * (1 - kotlin.math.abs((hue / 60f) % 2 - 1))
            val m = brightness - c

            val (r, g, b) = when ((hue / 60f).toInt() % 6) {
                0 -> Triple(c, x, 0f)
                1 -> Triple(x, c, 0f)
                2 -> Triple(0f, c, x)
                3 -> Triple(0f, x, c)
                4 -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }

            return SoCreateColor(r + m, g + m, b + m, alpha)
        }

        /** Create from Android color int (ARGB) */
        fun fromArgb(argb: Int): SoCreateColor {
            return SoCreateColor(
                red = ((argb shr 16) and 0xFF) / 255f,
                green = ((argb shr 8) and 0xFF) / 255f,
                blue = (argb and 0xFF) / 255f,
                alpha = ((argb shr 24) and 0xFF) / 255f
            )
        }
    }

    /** Convert to Android ARGB color int */
    fun toArgb(): Int {
        return (clamp01(alpha) * 255).toInt().shl(24) or
                (clamp01(red) * 255).toInt().shl(16) or
                (clamp01(green) * 255).toInt().shl(8) or
                (clamp01(blue) * 255).toInt()
    }

    /** Convert to HSV values */
    fun toHSB(): FloatArray {
        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val delta = max - min

        val hue = when {
            delta == 0f -> 0f
            max == red -> 60f * (((green - blue) / delta) % 6)
            max == green -> 60f * ((blue - red) / delta + 2)
            else -> 60f * ((red - green) / delta + 4)
        }.let { if (it < 0) it + 360f else it }

        val saturation = if (max == 0f) 0f else delta / max
        return floatArrayOf(hue, saturation, max)
    }

    fun withAlpha(newAlpha: Float) = copy(alpha = newAlpha)

    private fun clamp01(v: Float): Float = v.coerceIn(0f, 1f)
}

@Serializable
enum class ColorSpace {
    SRGB, LINEAR_SRGB, DISPLAY_P3
}
