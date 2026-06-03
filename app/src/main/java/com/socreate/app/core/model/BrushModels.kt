package com.socreate.app.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a single brush definition with all rendering parameters.
 * Inspired by Procreate's brush engine — each brush is a collection of
 * shape and grain textures combined with dynamic behaviors.
 */
@Serializable
data class Brush(
    val id: String,
    val name: String,
    val category: BrushCategory = BrushCategory.SKETCHING,
    val engine: BrushEngine = BrushEngine.STANDARD,
    val shape: BrushShape = BrushShape(),
    val grain: BrushGrain = BrushGrain(),
    val dynamics: BrushDynamics = BrushDynamics(),
    val properties: BrushProperties = BrushProperties(),
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Built-in brush IDs
        const val PENCIL_ID = "builtin_pencil"
        const val FINE_TIP_ID = "builtin_fine_tip"
        const val STUDIO_PEN_ID = "builtin_studio_pen"
        const val INK_BLEED_ID = "builtin_ink_bleed"
        const val DRY_INK_ID = "builtin_dry_ink"
        const val ROUND_BRUSH_ID = "builtin_round_brush"
        const val FLAT_BRUSH_ID = "builtin_flat_brush"
        const val SOFT_BRUSH_ID = "builtin_soft_brush"
        const val AIRBRUSH_ID = "builtin_airbrush"
        const val CHARCOAL_ID = "builtin_charcoal"
        const val HB_PENCIL_ID = "builtin_hb_pencil"
        const val TECHNICAL_PEN_ID = "builtin_technical_pen"
    }
}

@Serializable
enum class BrushCategory(val displayName: String) {
    SKETCHING("Sketching"),
    INKING("Inking"),
    PAINTING("Painting"),
    CALLIGRAPHY("Calligraphy"),
    TEXTURES("Textures"),
    AIRBRUSHING("Airbrushing"),
    CHARCOAL("Charcoal"),
    MARKERS("Markers"),
    CUSTOM("Custom"),
    ANIMATION("Animation") // SoCreate 2: Animation-optimized brushes
}

@Serializable
enum class BrushEngine {
    STANDARD,   // Basic rendering
    WET,        // Simulates wet media (watercolor, ink)
    DUAL,       // Combines two textures
    LIGHT,      // Light-based blending
    NOISE       // Noise/texture-based
}

@Serializable
data class BrushShape(
    val texture: BrushTexture = BrushTexture.ROUND,
    val scatter: Float = 0f,         // 0..1
    val rotation: Float = 0f,        // degrees
    val rotationJitter: Float = 0f,  // 0..1
    val sizeJitter: Float = 0f,      // 0..1
    val spacing: Float = 0.2f,       // 0..1 (overlap percentage)
    val aspectRatio: Float = 1f      // width/height
)

@Serializable
data class BrushGrain(
    val texture: BrushTexture = BrushTexture.NONE,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val blendMode: GrainBlendMode = GrainBlendMode.TEXTURE,
    val movement: Float = 0f
)

@Serializable
data class BrushDynamics(
    val sizePressure: PressureCurve = PressureCurve(),
    val opacityPressure: PressureCurve = PressureCurve(),
    val tiltSensitivity: Float = 0.5f,      // 0..1
    val velocitySensitivity: Float = 0f,     // 0..1
    val azmuthResponse: Boolean = false,
    val smoothing: Float = 0.5f,             // 0..1
    val stabilization: Stabilization = Stabilization.OFF
)

@Serializable
data class BrushProperties(
    val baseSize: Float = 20f,              // px
    val minSize: Float = 1f,                // px
    val opacity: Float = 1f,                // 0..1
    val minOpacity: Float = 0.01f,          // 0..1
    val blendMode: BlendMode = BlendMode.NORMAL,
    val accumulation: Float = 0f,           // 0..1 - color build-up
    val wetness: Float = 0f,                // 0..1 - wet media simulation
    val dilution: Float = 0f,               // 0..1 - how much color is diluted
    val colorDynamics: ColorDynamics = ColorDynamics()
)

@Serializable
data class PressureCurve(
    val points: List<ControlPoint> = listOf(
        ControlPoint(0f, 0f),
        ControlPoint(1f, 1f)
    )
) {
    /** Evaluate the curve at a given pressure value (0..1) using cubic interpolation */
    fun evaluate(pressure: Float): Float {
        if (pressure <= 0f) return points.first().y
        if (pressure >= 1f) return points.last().y

        // Find surrounding control points
        var lower = points.first()
        var upper = points.last()
        for (i in 0 until points.size - 1) {
            if (pressure >= points[i].x && pressure <= points[i + 1].x) {
                lower = points[i]
                upper = points[i + 1]
                break
            }
        }

        // Linear interpolation between control points
        val t = if (upper.x == lower.x) 0f else (pressure - lower.x) / (upper.x - lower.x)
        return lower.y + (upper.y - lower.y) * t
    }
}

@Serializable
data class ControlPoint(val x: Float, val y: Float)

@Serializable
enum class BrushTexture {
    NONE, ROUND, SOFT_ROUND, FLAT, ROUGH, CHALK, CHARCOAL,
    SPONGE, FABRIC, PAPER, NOISE, PATTERN_1, PATTERN_2,
    WATERCOLOR, OIL, ACRYLIC, CUSTOM
}

@Serializable
enum class GrainBlendMode {
    TEXTURE, STAMP, CLIPPING, PATTERN
}

@Serializable
enum class Stabilization(val maxPoints: Int) {
    OFF(0), LIGHT(10), MEDIUM(25), HEAVY(50), MAX(100)
}

@Serializable
enum class BlendMode(val displayName: String) {
    NORMAL("Normal"),
    MULTIPLY("Multiply"),
    SCREEN("Screen"),
    OVERLAY("Overlay"),
    DARKEN("Darken"),
    LIGHTEN("Lighten"),
    COLOR_DODGE("Color Dodge"),
    COLOR_BURN("Color Burn"),
    HARD_LIGHT("Hard Light"),
    SOFT_LIGHT("Soft Light"),
    DIFFERENCE("Difference"),
    EXCLUSION("Exclusion"),
    HUE("Hue"),
    SATURATION("Saturation"),
    COLOR("Color"),
    LUMINOSITY("Luminosity"),
    // Wet media blend modes
    WET("Wet"),
    WET_DARK("Wet Dark"),
    WET_LIGHT("Wet Light"),
    // SoCreate special modes
    GLAZE("Glaze"),
    UNDERPAINT("Underpaint")
}

@Serializable
data class ColorDynamics(
    val hueShift: Float = 0f,        // 0..1
    val saturationShift: Float = 0f,  // 0..1
    val brightnessShift: Float = 0f,  // 0..1
    val perStroke: Boolean = false,
    val perDab: Boolean = false
)
