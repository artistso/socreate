package com.socreate.app.engine.renderer

import android.graphics.*
import com.socreate.app.core.model.*
import kotlin.math.*

/**
 * Smart Shape detection and rendering.
 * Inspired by Clip Studio Paint v5's Smart Shape feature.
 *
 * Detects geometric intent from freehand strokes and snaps them
 * to perfect shapes without switching tools.
 *
 * Detection algorithm:
 * 1. Calculate path metrics (circularity, straightness, corner detection)
 * 2. Classify shape intent (circle, rectangle, line, triangle, etc.)
 * 3. Fit the ideal shape to the user's stroke
 * 4. Render with the current brush texture preserved
 */
class SmartShapeDetector {

    /**
     * Analyze a freehand stroke and detect if it's an attempted shape.
     * Returns a SmartShapeResult if a shape was detected, null otherwise.
     */
    fun detectShape(stroke: Stroke): SmartShapeResult? {
        val points = stroke.points
        if (points.size < 5) return null

        val bounds = stroke.bounds
        val pathLength = calculatePathLength(points)
        val diagonal = sqrt(bounds.width * bounds.width + bounds.height * bounds.height)

        if (diagonal < 10f) return null

        // Check if stroke closes on itself (start ≈ end)
        val startEnd = distance(points.first(), points.last())
        val isClosed = startEnd < diagonal * 0.2f

        // Calculate straightness
        val straightness = diagonal / pathLength

        // Calculate circularity (perfect circle = 1.0)
        val area = bounds.width * bounds.height
        val circularity = if (pathLength > 0) (4 * PI * area) / (pathLength * pathLength) else 0.0

        // Detect corners
        val corners = detectCorners(points)

        return when {
            // Straight line (straightness > 0.85)
            straightness > 0.85f && !isClosed -> {
                SmartShapeResult(
                    type = SmartShapeType.LINE,
                    confidence = straightness,
                    points = listOf(points.first(), points.last())
                )
            }

            // Circle / ellipse (high circularity, closed path)
            circularity > 0.65 && isClosed -> {
                SmartShapeResult(
                    type = SmartShapeType.ELLIPSE,
                    confidence = circularity.toFloat().coerceIn(0f, 1f),
                    bounds = bounds
                )
            }

            // Rectangle (4 corners, closed, roughly perpendicular)
            corners.size == 4 && isClosed -> {
                SmartShapeResult(
                    type = SmartShapeType.RECTANGLE,
                    confidence = calculateRectangleConfidence(points, corners),
                    bounds = fitBoundsToCorners(corners)
                )
            }

            // Triangle (3 corners, closed)
            corners.size == 3 && isClosed -> {
                SmartShapeResult(
                    type = SmartShapeType.POLYGON,
                    confidence = 0.7f,
                    vertices = corners
                )
            }

            // No shape detected with sufficient confidence
            else -> null
        }
    }

    /**
     * Draw the detected smart shape onto the canvas using the given paint.
     * Preserves brush texture from the SmartShape config.
     */
    fun drawShape(
        canvas: Canvas,
        result: SmartShapeResult,
        config: SmartShape,
        paint: Paint
    ) {
        when (result.type) {
            SmartShapeType.LINE -> {
                val pts = result.points
                if (pts != null && pts.size >= 2) {
                    canvas.drawLine(pts[0].x, pts[0].y, pts[1].x, pts[1].y, paint)
                }
            }
            SmartShapeType.RECTANGLE -> {
                val b = result.bounds ?: return
                val rect = RectF(b.x, b.y, b.right, b.bottom)
                if (config.fillAfterClose && config.fillColor.alpha > 0) {
                    val fillPaint = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = config.fillColor.toArgb()
                    }
                    canvas.drawRect(rect, fillPaint)
                }
                canvas.drawRect(rect, paint)
            }
            SmartShapeType.ELLIPSE -> {
                val b = result.bounds ?: return
                val rect = RectF(b.x, b.y, b.right, b.bottom)
                if (config.fillAfterClose && config.fillColor.alpha > 0) {
                    val fillPaint = Paint(paint).apply {
                        style = Paint.Style.FILL
                        color = config.fillColor.toArgb()
                    }
                    canvas.drawOval(rect, fillPaint)
                }
                canvas.drawOval(rect, paint)
            }
            SmartShapeType.POLYGON -> {
                val verts = result.vertices ?: return
                val path = Path()
                path.moveTo(verts[0].x, verts[0].y)
                for (i in 1 until verts.size) {
                    path.lineTo(verts[i].x, verts[i].y)
                }
                path.close()
                canvas.drawPath(path, paint)
            }
            else -> { /* Freehand not rendered as smart shape */ }
        }
    }

    // ─── Shape Detection Helpers ─────────────────────────────────────────

    private fun calculatePathLength(points: List<StrokePoint>): Float {
        var length = 0f
        for (i in 1 until points.size) {
            length += distance(points[i - 1], points[i])
        }
        return length
    }

    private fun distance(a: StrokePoint, b: StrokePoint): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * Detect corner points using angle-based analysis.
     */
    private fun detectCorners(points: List<StrokePoint>): List<StrokePoint> {
        val corners = mutableListOf<StrokePoint>()
        val windowSize = max(5, points.size / 20)

        for (i in windowSize until points.size - windowSize) {
            val prev = points[i - windowSize]
            val curr = points[i]
            val next = points[i + windowSize]

            val angle = calculateAngle(prev, curr, next)

            // Sharp angle = corner
            if (angle < 140f) {
                corners.add(curr)
            }
        }

        // Merge nearby corners
        return mergeNearbyCorners(corners)
    }

    private fun calculateAngle(a: StrokePoint, b: StrokePoint, c: StrokePoint): Float {
        val ab = floatArrayOf(a.x - b.x, a.y - b.y)
        val cb = floatArrayOf(c.x - b.x, c.y - b.y)

        val dot = ab[0] * cb[0] + ab[1] * cb[1]
        val magAB = sqrt(ab[0] * ab[0] + ab[1] * ab[1])
        val magCB = sqrt(cb[0] * cb[0] + cb[1] * cb[1])

        if (magAB == 0f || magCB == 0f) return 180f

        val cosAngle = (dot / (magAB * magCB)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cosAngle)).toFloat()
    }

    private fun mergeNearbyCorners(corners: List<StrokePoint>): List<StrokePoint> {
        if (corners.isEmpty()) return corners

        val merged = mutableListOf<StrokePoint>()
        var current = corners.first()

        for (i in 1 until corners.size) {
            val dist = distance(current, corners[i])
            if (dist < 30f) {
                // Average the two corners
                current = current.copy(
                    x = (current.x + corners[i].x) / 2,
                    y = (current.y + corners[i].y) / 2
                )
            } else {
                merged.add(current)
                current = corners[i]
            }
        }
        merged.add(current)
        return merged
    }

    private fun calculateRectangleConfidence(
        points: List<StrokePoint>,
        corners: List<StrokePoint>
    ): Float {
        if (corners.size != 4) return 0f

        // Check perpendicularity of adjacent sides
        var totalDeviation = 0f
        for (i in corners.indices) {
            val a = corners[i]
            val b = corners[(i + 1) % 4]
            val c = corners[(i + 2) % 4]
            val angle = calculateAngle(a, b, c)
            totalDeviation += abs(angle - 90f)
        }

        val avgDeviation = totalDeviation / 4f
        return (1f - avgDeviation / 90f).coerceIn(0f, 1f)
    }

    private fun fitBoundsToCorners(corners: List<StrokePoint>): Bounds {
        val minX = corners.minOf { it.x }
        val minY = corners.minOf { it.y }
        val maxX = corners.maxOf { it.x }
        val maxY = corners.maxOf { it.y }
        return Bounds(minX, minY, maxX - minX, maxY - minY)
    }
}

/**
 * Result of smart shape detection.
 */
data class SmartShapeResult(
    val type: SmartShapeType,
    val confidence: Float,                  // 0..1 how confident the detection is
    val bounds: Bounds? = null,
    val points: List<StrokePoint>? = null,  // For line type (2 points)
    val vertices: List<StrokePoint>? = null // For polygon types
)
