package com.socreate.app.engine.brush

import com.socreate.app.core.model.*

/**
 * Built-in brush library with 13 professionally tuned brushes.
 * Each brush is carefully configured with specific dynamics,
 * shapes, and properties for a natural drawing feel.
 */
object BrushPresets {

    val allBrushes: List<Brush> by lazy {
        listOf(
            // ─── Sketching ────────────────────────────────────────────
            hbPencil(),
            pencil(),
            fineTip(),
            charcoal(),

            // ─── Inking ──────────────────────────────────────────────
            studioPen(),
            technicalPen(),
            inkBleed(),
            dryInk(),

            // ─── Painting ────────────────────────────────────────────
            roundBrush(),
            flatBrush(),
            softBrush(),

            // ─── Airbrushing ─────────────────────────────────────────
            airbrush(),

            // ─── Calligraphy ─────────────────────────────────────────
            calligraphyBrush()
        )
    }

    fun getBrushById(id: String): Brush? = allBrushes.find { it.id == id }

    fun getBrushesByCategory(category: BrushCategory): List<Brush> =
        allBrushes.filter { it.category == category }

    // ─── Sketching Brushes ──────────────────────────────────────────────

    private fun hbPencil() = Brush(
        id = Brush.HB_PENCIL_ID,
        name = "HB Pencil",
        category = BrushCategory.SKETCHING,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.12f,
            sizeJitter = 0.03f,
            rotationJitter = 0.02f
        ),
        grain = BrushGrain(
            texture = BrushTexture.PAPER,
            scale = 0.8f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.1f),
                    ControlPoint(0.3f, 0.3f),
                    ControlPoint(0.7f, 0.7f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.3f),
                    ControlPoint(0.5f, 0.7f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.2f,
            tiltSensitivity = 0.4f
        ),
        properties = BrushProperties(
            baseSize = 12f,
            minSize = 1f,
            opacity = 0.85f,
            minOpacity = 0.1f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.1f
        )
    )

    private fun pencil() = Brush(
        id = Brush.PENCIL_ID,
        name = "Pencil",
        category = BrushCategory.SKETCHING,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.ROUGH,
            spacing = 0.15f,
            sizeJitter = 0.05f,
            scatter = 0.01f
        ),
        grain = BrushGrain(
            texture = BrushTexture.PAPER,
            scale = 1.0f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.05f),
                    ControlPoint(0.4f, 0.25f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.15f),
                    ControlPoint(1f, 0.9f)
                )
            ),
            smoothing = 0.3f,
            tiltSensitivity = 0.3f
        ),
        properties = BrushProperties(
            baseSize = 8f,
            minSize = 1f,
            opacity = 0.7f,
            minOpacity = 0.05f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.15f
        )
    )

    private fun fineTip() = Brush(
        id = Brush.FINE_TIP_ID,
        name = "Fine Tip",
        category = BrushCategory.SKETCHING,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.08f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.2f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.5f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.15f
        ),
        properties = BrushProperties(
            baseSize = 4f,
            minSize = 1f,
            opacity = 0.9f,
            blendMode = BlendMode.NORMAL
        )
    )

    private fun charcoal() = Brush(
        id = Brush.CHARCOAL_ID,
        name = "Charcoal",
        category = BrushCategory.CHARCOAL,
        engine = com.socreate.app.core.model.BrushEngine.NOISE,
        shape = BrushShape(
            texture = BrushTexture.CHARCOAL,
            spacing = 0.25f,
            sizeJitter = 0.15f,
            scatter = 0.08f,
            rotationJitter = 0.3f,
            aspectRatio = 0.6f
        ),
        grain = BrushGrain(
            texture = BrushTexture.ROUGH,
            scale = 1.5f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.3f),
                    ControlPoint(0.5f, 0.6f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.2f),
                    ControlPoint(1f, 0.8f)
                )
            ),
            tiltSensitivity = 0.6f,
            velocitySensitivity = 0.1f,
            smoothing = 0.4f
        ),
        properties = BrushProperties(
            baseSize = 24f,
            minSize = 4f,
            opacity = 0.6f,
            minOpacity = 0.05f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.2f
        )
    )

    // ─── Inking Brushes ──────────────────────────────────────────────────

    private fun studioPen() = Brush(
        id = Brush.STUDIO_PEN_ID,
        name = "Studio Pen",
        category = BrushCategory.INKING,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.05f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.05f),
                    ControlPoint(0.3f, 0.2f),
                    ControlPoint(0.8f, 0.8f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.1f
        ),
        properties = BrushProperties(
            baseSize = 12f,
            minSize = 1f,
            opacity = 1f,
            blendMode = BlendMode.NORMAL
        )
    )

    private fun technicalPen() = Brush(
        id = Brush.TECHNICAL_PEN_ID,
        name = "Technical Pen",
        category = BrushCategory.INKING,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.04f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 1f),
                    ControlPoint(1f, 1f)
                ) // Fixed size
            ),
            smoothing = 0.05f
        ),
        properties = BrushProperties(
            baseSize = 3f,
            minSize = 1f,
            opacity = 1f,
            blendMode = BlendMode.NORMAL
        )
    )

    private fun inkBleed() = Brush(
        id = Brush.INK_BLEED_ID,
        name = "Ink Bleed",
        category = BrushCategory.INKING,
        engine = com.socreate.app.core.model.BrushEngine.WET,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.1f,
            sizeJitter = 0.03f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.1f),
                    ControlPoint(0.4f, 0.5f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.2f
        ),
        properties = BrushProperties(
            baseSize = 14f,
            minSize = 2f,
            opacity = 0.85f,
            blendMode = BlendMode.WET,
            wetness = 0.4f,
            dilution = 0.2f,
            accumulation = 0.3f
        )
    )

    private fun dryInk() = Brush(
        id = Brush.DRY_INK_ID,
        name = "Dry Ink",
        category = BrushCategory.INKING,
        engine = com.socreate.app.core.model.BrushEngine.NOISE,
        shape = BrushShape(
            texture = BrushTexture.ROUGH,
            spacing = 0.18f,
            sizeJitter = 0.08f,
            scatter = 0.03f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.15f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.25f
        ),
        properties = BrushProperties(
            baseSize = 10f,
            minSize = 1f,
            opacity = 0.9f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.1f
        )
    )

    // ─── Painting Brushes ────────────────────────────────────────────────

    private fun roundBrush() = Brush(
        id = Brush.ROUND_BRUSH_ID,
        name = "Round Brush",
        category = BrushCategory.PAINTING,
        engine = com.socreate.app.core.model.BrushEngine.WET,
        shape = BrushShape(
            texture = BrushTexture.ROUND,
            spacing = 0.1f,
            sizeJitter = 0.02f
        ),
        grain = BrushGrain(
            texture = BrushTexture.FABRIC,
            scale = 1.2f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.2f),
                    ControlPoint(0.5f, 0.6f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.4f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.15f,
            tiltSensitivity = 0.2f
        ),
        properties = BrushProperties(
            baseSize = 30f,
            minSize = 2f,
            opacity = 0.8f,
            minOpacity = 0.1f,
            blendMode = BlendMode.WET,
            wetness = 0.3f,
            dilution = 0.15f,
            accumulation = 0.25f
        )
    )

    private fun flatBrush() = Brush(
        id = Brush.FLAT_BRUSH_ID,
        name = "Flat Brush",
        category = BrushCategory.PAINTING,
        engine = com.socreate.app.core.model.BrushEngine.WET,
        shape = BrushShape(
            texture = BrushTexture.FLAT,
            spacing = 0.15f,
            aspectRatio = 0.3f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.3f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.2f,
            azmuthResponse = true,
            tiltSensitivity = 0.5f
        ),
        properties = BrushProperties(
            baseSize = 40f,
            minSize = 4f,
            opacity = 0.85f,
            blendMode = BlendMode.WET,
            wetness = 0.25f,
            accumulation = 0.2f
        )
    )

    private fun softBrush() = Brush(
        id = Brush.SOFT_BRUSH_ID,
        name = "Soft Brush",
        category = BrushCategory.PAINTING,
        engine = com.socreate.app.core.model.BrushEngine.LIGHT,
        shape = BrushShape(
            texture = BrushTexture.SOFT_ROUND,
            spacing = 0.08f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.4f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.35f
        ),
        properties = BrushProperties(
            baseSize = 50f,
            minSize = 5f,
            opacity = 0.3f,
            minOpacity = 0.01f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.5f
        )
    )

    // ─── Airbrushing ────────────────────────────────────────────────────

    private fun airbrush() = Brush(
        id = Brush.AIRBRUSH_ID,
        name = "Airbrush",
        category = BrushCategory.AIRBRUSHING,
        engine = com.socreate.app.core.model.BrushEngine.LIGHT,
        shape = BrushShape(
            texture = BrushTexture.SOFT_ROUND,
            spacing = 0.05f,
            scatter = 0.1f,
            sizeJitter = 0.05f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.5f),
                    ControlPoint(1f, 1f)
                )
            ),
            opacityPressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.1f),
                    ControlPoint(0.5f, 0.3f),
                    ControlPoint(1f, 0.6f)
                )
            ),
            smoothing = 0.5f
        ),
        properties = BrushProperties(
            baseSize = 60f,
            minSize = 10f,
            opacity = 0.15f,
            minOpacity = 0.01f,
            blendMode = BlendMode.NORMAL,
            accumulation = 0.6f
        )
    )

    // ─── Calligraphy ────────────────────────────────────────────────────

    private fun calligraphyBrush() = Brush(
        id = "builtin_calligraphy",
        name = "Calligraphy",
        category = BrushCategory.CALLIGRAPHY,
        engine = com.socreate.app.core.model.BrushEngine.STANDARD,
        shape = BrushShape(
            texture = BrushTexture.FLAT,
            spacing = 0.06f,
            aspectRatio = 0.15f
        ),
        dynamics = BrushDynamics(
            sizePressure = PressureCurve(
                points = listOf(
                    ControlPoint(0f, 0.1f),
                    ControlPoint(0.5f, 0.5f),
                    ControlPoint(1f, 1f)
                )
            ),
            smoothing = 0.1f,
            azmuthResponse = true,
            tiltSensitivity = 0.7f
        ),
        properties = BrushProperties(
            baseSize = 16f,
            minSize = 1f,
            opacity = 1f,
            blendMode = BlendMode.NORMAL
        )
    )
}
