package com.socreate.app.engine.renderer

import android.graphics.*
import android.graphics.Paint as AndroidPaint
import com.socreate.app.core.model.*

/**
 * High-performance stroke renderer.
 * Converts Stroke data into Canvas draw calls with proper brush simulation.
 *
 * Uses path-based rendering with pressure-sensitive width interpolation
 * and optimized tile-based rendering for large canvases.
 */
class StrokeRenderer {

    private val strokePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val dabPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        style = AndroidPaint.Style.FILL
    }

    private val path = Path()
    private val tempMatrix = Matrix()
    private val tempRect = RectF()

    /**
     * Render a complete stroke onto the given canvas.
     * Uses the appropriate rendering strategy based on brush settings.
     */
    fun renderStroke(
        canvas: Canvas,
        stroke: Stroke,
        brush: Brush,
        viewportBounds: Bounds? = null
    ) {
        if (stroke.isEmpty) return

        // Early culling: skip if stroke is outside viewport
        if (viewportBounds != null && !stroke.bounds.intersects(viewportBounds)) {
            return
        }

        when (brush.engine) {
            com.socreate.app.core.model.BrushEngine.STANDARD -> renderStandardStroke(canvas, stroke, brush)
            com.socreate.app.core.model.BrushEngine.WET -> renderWetStroke(canvas, stroke, brush)
            com.socreate.app.core.model.BrushEngine.DUAL -> renderDualStroke(canvas, stroke, brush)
            com.socreate.app.core.model.BrushEngine.LIGHT -> renderLightStroke(canvas, stroke, brush)
            com.socreate.app.core.model.BrushEngine.NOISE -> renderNoiseStroke(canvas, stroke, brush)
        }
    }

    /**
     * Standard brush rendering: smooth path with pressure-based width variation.
     */
    private fun renderStandardStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        val points = stroke.points
        if (points.isEmpty()) return

        val color = stroke.color.toArgb()

        // Single point (tap)
        if (points.size == 1) {
            val p = points[0]
            val size = calculateSize(p.pressure, brush.properties) * 2f
            dabPaint.color = color
            dabPaint.alpha = (calculateOpacity(p.pressure, brush.properties) * 255).toInt()
            canvas.drawCircle(p.x, p.y, size / 2f, dabPaint)
            return
        }

        // Multi-point stroke: render as segments with varying width
        if (brush.shape.spacing >= 0.5f) {
            // High spacing: render individual dabs
            renderDabBasedStroke(canvas, stroke, brush)
        } else {
            // Low spacing: render as smooth path segments
            renderPathBasedStroke(canvas, stroke, brush)
        }
    }

    /**
     * Path-based rendering for smooth brushes (low spacing).
     * Breaks stroke into segments with interpolated width.
     */
    private fun renderPathBasedStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        val points = stroke.points
        val color = stroke.color.toArgb()

        // Render in segments of similar width for smooth appearance
        var i = 0
        while (i < points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val p2 = if (i + 2 < points.size) points[i + 2] else null
            val p3 = if (i + 3 < points.size) points[i + 3] else null

            val avgPressure = (p0.pressure + p1.pressure) / 2f
            val size = calculateSize(avgPressure, brush.properties)
            val opacity = calculateOpacity(avgPressure, brush.properties)

            strokePaint.strokeWidth = size
            strokePaint.color = color
            strokePaint.alpha = (opacity * 255).toInt()

            path.reset()
            path.moveTo(p0.x, p0.y)

            if (p2 != null) {
                // Use quadratic Bezier for smooth curves
                val midX = (p1.x + p2.x) / 2f
                val midY = (p1.y + p2.y) / 2f
                path.quadTo(p1.x, p1.y, midX, midY)
            } else {
                path.lineTo(p1.x, p1.y)
            }

            canvas.drawPath(path, strokePaint)
            i++
        }
    }

    /**
     * Dab-based rendering for textured brushes (high spacing).
     * Renders individual brush stamps along the path.
     */
    private fun renderDabBasedStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        val points = stroke.points
        val spacing = brush.shape.spacing.coerceIn(0.05f, 1f)
        val baseSize = brush.properties.baseSize

        for (i in points.indices) {
            val p = points[i]
            val size = calculateSize(p.pressure, brush.properties)
            val opacity = calculateOpacity(p.pressure, brush.properties)

            // Apply jitter
            val jitterX = if (brush.shape.sizeJitter > 0) {
                (Math.random() - 0.5f) * size * brush.shape.sizeJitter
            } else 0f

            val jitterY = if (brush.shape.sizeJitter > 0) {
                (Math.random() - 0.5f) * size * brush.shape.sizeJitter
            } else 0f

            val scatterX = if (brush.shape.scatter > 0) {
                (Math.random() - 0.5f) * baseSize * 4f * brush.shape.scatter
            } else 0f

            val scatterY = if (brush.shape.scatter > 0) {
                (Math.random() - 0.5f) * baseSize * 4f * brush.shape.scatter
            } else 0f

            val dabX = p.x + jitterX + scatterX
            val dabY = p.y + jitterY + scatterY

            renderDab(canvas, brush, stroke.color, dabX, dabY, size, opacity, p.orientation)
        }
    }

    /**
     * Render a single brush dab (stamp).
     */
    private fun renderDab(
        canvas: Canvas,
        brush: Brush,
        color: SoCreateColor,
        x: Float,
        y: Float,
        size: Float,
        opacity: Float,
        orientation: Float
    ) {
        dabPaint.color = color.toArgb()
        dabPaint.alpha = (opacity * 255).toInt()

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(Math.toDegrees(orientation.toDouble()).toFloat())

        when (brush.shape.texture) {
            BrushTexture.ROUND,
            BrushTexture.SOFT_ROUND -> {
                // Soft brush with radial gradient
                if (brush.shape.texture == BrushTexture.SOFT_ROUND) {
                    val radius = size / 2f
                    val gradient = RadialGradient(
                        0f, 0f, radius,
                        color.toArgb(),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                    dabPaint.shader = gradient
                }
                canvas.drawCircle(0f, 0f, size / 2f, dabPaint)
                dabPaint.shader = null
            }
            BrushTexture.FLAT -> {
                val halfW = size / 2f * brush.shape.aspectRatio
                val halfH = size / 2f
                canvas.drawOval(
                    -halfW, -halfH, halfW, halfH,
                    dabPaint
                )
            }
            BrushTexture.CHARCOAL,
            BrushTexture.ROUGH -> {
                // Textured dab with noise
                canvas.drawCircle(0f, 0f, size / 2f, dabPaint)
                // Add texture overlay
                renderTextureOverlay(canvas, brush, size, opacity * 0.3f)
            }
            else -> {
                canvas.drawCircle(0f, 0f, size / 2f, dabPaint)
            }
        }

        canvas.restore()
    }

    /**
     * Render a texture overlay on top of a dab for grainy/textured brushes.
     */
    private fun renderTextureOverlay(
        canvas: Canvas,
        brush: Brush,
        size: Float,
        opacity: Float
    ) {
        val grainSize = brush.grain.scale.coerceIn(0.1f, 10f)
        val count = (size * grainSize / 4).toInt().coerceIn(2, 20)

        val grainPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            style = AndroidPaint.Style.FILL
            alpha = (opacity * 255).toInt()
            color = Color.TRANSPARENT
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
        }

        for (i in 0 until count) {
            val angle = Math.random() * Math.PI * 2
            val dist = Math.random() * size / 2f
            val gx = (Math.cos(angle) * dist).toFloat()
            val gy = (Math.sin(angle) * dist).toFloat()
            val gr = (Math.random() * 2f + 0.5f).toFloat()
            canvas.drawCircle(gx, gy, gr, grainPaint)
        }
    }

    // ─── Wet Media Rendering ────────────────────────────────────────────────

    private fun renderWetStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        // Wet media simulation: color blending, diffusion
        // For now, use standard rendering with wet blend mode
        strokePaint.xfermode = when (brush.properties.blendMode) {
            BlendMode.WET -> PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
            BlendMode.WET_DARK -> PorterDuffXfermode(PorterDuff.Mode.DARKEN)
            BlendMode.WET_LIGHT -> PorterDuffXfermode(PorterDuff.Mode.LIGHTEN)
            BlendMode.GLAZE -> PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
            else -> null
        }

        renderStandardStroke(canvas, stroke, brush)
        strokePaint.xfermode = null
    }

    private fun renderDualStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        // Dual engine: render shape first, then grain on top
        renderStandardStroke(canvas, stroke, brush)
        // TODO: Implement grain overlay pass
    }

    private fun renderLightStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        strokePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SCREEN)
        renderStandardStroke(canvas, stroke, brush)
        strokePaint.xfermode = null
    }

    private fun renderNoiseStroke(canvas: Canvas, stroke: Stroke, brush: Brush) {
        renderDabBasedStroke(canvas, stroke, brush.copy(
            shape = brush.shape.copy(scatter = brush.shape.scatter + 0.2f)
        ))
    }

    // ─── Utility ────────────────────────────────────────────────────────────

    private fun calculateSize(pressure: Float, properties: BrushProperties): Float {
        val minSize = properties.minSize
        val baseSize = properties.baseSize
        return minSize + (baseSize - minSize) * pressure
    }

    private fun calculateOpacity(pressure: Float, properties: BrushProperties): Float {
        val minOpacity = properties.minOpacity
        val maxOpacity = properties.opacity
        return minOpacity + (maxOpacity - minOpacity) * pressure
    }

    /**
     * Render an in-progress stroke (while the user is drawing).
     * Optimized for real-time performance — only renders new points.
     */
    fun renderIncrementalStroke(
        canvas: Canvas,
        stroke: Stroke,
        brush: Brush,
        fromIndex: Int
    ) {
        if (fromIndex >= stroke.points.size) return

        val points = stroke.points
        val color = stroke.color.toArgb()

        for (i in fromIndex until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]

            val avgPressure = (p0.pressure + p1.pressure) / 2f
            val size = calculateSize(avgPressure, brush.properties)
            val opacity = calculateOpacity(avgPressure, brush.properties)

            strokePaint.strokeWidth = size
            strokePaint.color = color
            strokePaint.alpha = (opacity * 255).toInt()

            canvas.drawLine(p0.x, p0.y, p1.x, p1.y, strokePaint)
        }
    }
}
