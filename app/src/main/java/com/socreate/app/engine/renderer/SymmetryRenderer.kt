package com.socreate.app.engine.renderer

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import com.socreate.app.core.model.StrokePoint
import com.socreate.app.core.model.SymmetryConfig
import com.socreate.app.core.model.SymmetryType

/**
 * Renders symmetry guides and replicates strokes across symmetry axes.
 * Inspired by HiPaint's symmetry drawing feature.
 *
 * Supports:
 * - Vertical / Horizontal mirror
 * - Quadrant (4-way)
 * - Radial (N-fold rotational)
 * - Mandala (radial + mirror)
 * - Custom angle
 */
class SymmetryRenderer {

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f
        pathEffect = DashPathEffect(floatArrayOf(8f, 4f), 0f)
    }

    /**
     * Draw symmetry guide lines on the canvas.
     */
    fun drawGuides(canvas: Canvas, config: SymmetryConfig, canvasWidth: Int, canvasHeight: Int) {
        if (!config.isEnabled || !config.showGuides) return

        guidePaint.color = config.guideColor.toArgb()
        guidePaint.alpha = (config.guideColor.alpha * 255).toInt()

        val cx = canvasWidth * config.guidePosition
        val cy = canvasHeight * config.guidePosition

        when (config.type) {
            SymmetryType.VERTICAL -> {
                canvas.drawLine(cx, 0f, cx, canvasHeight.toFloat(), guidePaint)
            }
            SymmetryType.HORIZONTAL -> {
                canvas.drawLine(0f, cy, canvasWidth.toFloat(), cy, guidePaint)
            }
            SymmetryType.QUADRANT -> {
                canvas.drawLine(cx, 0f, cx, canvasHeight.toFloat(), guidePaint)
                canvas.drawLine(0f, cy, canvasWidth.toFloat(), cy, guidePaint)
            }
            SymmetryType.RADIAL, SymmetryType.ROTATIONAL, SymmetryType.MANDALA -> {
                val count = config.rotationalCount
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f
                val radius = kotlin.math.sqrt(
                    (centerX * centerX + centerY * centerY).toDouble()
                ).toFloat()

                for (i in 0 until count) {
                    val angle = (360f / count) * i
                    canvas.save()
                    canvas.rotate(angle, centerX, centerY)
                    canvas.drawLine(centerX, centerY, centerX, centerY - radius, guidePaint)
                    canvas.restore()
                }
            }
            SymmetryType.CUSTOM_ANGLE -> {
                // Custom angle symmetry line through center
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f
                val angle = config.guidePosition * 180f // Map 0..1 to 0..180 degrees
                canvas.save()
                canvas.rotate(angle, centerX, centerY)
                canvas.drawLine(centerX, 0f, centerX, canvasHeight.toFloat(), guidePaint)
                canvas.restore()
            }
        }
    }

    /**
     * Generate mirrored copies of a stroke point based on symmetry config.
     * Returns the original point plus all mirrored copies.
     */
    fun mirrorPoint(
        point: StrokePoint,
        config: SymmetryConfig,
        canvasWidth: Int,
        canvasHeight: Int
    ): List<StrokePoint> {
        if (!config.isEnabled) return listOf(point)

        val cx = canvasWidth * config.guidePosition
        val cy = canvasHeight * config.guidePosition
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        return when (config.type) {
            SymmetryType.VERTICAL -> {
                listOf(point, point.copy(x = 2 * cx - point.x))
            }
            SymmetryType.HORIZONTAL -> {
                listOf(point, point.copy(y = 2 * cy - point.y))
            }
            SymmetryType.QUADRANT -> {
                listOf(
                    point,
                    point.copy(x = 2 * cx - point.x),                    // V mirror
                    point.copy(y = 2 * cy - point.y),                    // H mirror
                    point.copy(x = 2 * cx - point.x, y = 2 * cy - point.y) // Both
                )
            }
            SymmetryType.RADIAL -> {
                generateRadialCopies(point, config.rotationalCount, centerX, centerY)
            }
            SymmetryType.ROTATIONAL -> {
                generateRadialCopies(point, config.rotationalCount, centerX, centerY)
            }
            SymmetryType.MANDALA -> {
                val radial = generateRadialCopies(point, config.rotationalCount, centerX, centerY)
                // Add vertical mirror of each radial copy
                radial + radial.map { it.copy(x = 2 * centerX - it.x) }
            }
            SymmetryType.CUSTOM_ANGLE -> {
                val angle = config.guidePosition * Math.PI // Radians
                val dx = point.x - centerX
                val dy = point.y - centerY
                val mirrorX = (dx * kotlin.math.cos(2 * angle) + dy * kotlin.math.sin(2 * angle)).toFloat() + centerX
                val mirrorY = (dx * kotlin.math.sin(2 * angle) - dy * kotlin.math.cos(2 * angle)).toFloat() + centerY
                listOf(point, point.copy(x = mirrorX, y = mirrorY))
            }
        }
    }

    private fun generateRadialCopies(
        point: StrokePoint,
        count: Int,
        centerX: Float,
        centerY: Float
    ): List<StrokePoint> {
        val dx = point.x - centerX
        val dy = point.y - centerY
        val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())

        return (0 until count).map { i ->
            val newAngle = angle + (2 * Math.PI * i / count)
            val newX = (kotlin.math.cos(newAngle) * dx - kotlin.math.sin(newAngle) * dy).toFloat() + centerX
            val newY = (kotlin.math.sin(newAngle) * dx + kotlin.math.cos(newAngle) * dy).toFloat() + centerY
            point.copy(x = newX, y = newY)
        }
    }
}
