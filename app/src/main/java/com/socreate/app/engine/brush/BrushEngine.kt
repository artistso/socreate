package com.socreate.app.engine.brush

import com.socreate.app.core.model.*
import java.util.LinkedList
import kotlin.math.*

/**
 * Core brush engine that processes raw input points into smooth,
 * pressure-responsive stroke data ready for rendering.
 *
 * Handles:
 * - Input smoothing (catmull-rom / moving average)
 * - Pressure curve mapping
 * - Tilt compensation
 * - Velocity-based dynamics
 * - Stabilization (for shaky hands)
 */
class BrushEngine {

    /**
     * Process raw input points into smooth stroke points.
     * Called for each new point as the user draws.
     */
    fun processPoint(
        rawPoint: StrokePoint,
        brush: Brush,
        previousPoints: List<StrokePoint>,
        engineState: BrushEngineState
    ): List<StrokePoint> {
        val state = engineState

        // 1. Apply stabilization (collects points before emitting)
        if (brush.dynamics.stabilization != Stabilization.OFF) {
            state.stabilizerBuffer.add(rawPoint)
            if (state.stabilizerBuffer.size < brush.dynamics.stabilization.maxPoints) {
                return emptyList() // Buffer not full yet
            }
        } else {
            state.stabilizerBuffer.add(rawPoint)
        }

        // 2. Apply smoothing
        val smoothed = applySmoothing(
            rawPoint,
            previousPoints,
            brush.dynamics.smoothing
        )

        // 3. Apply pressure curve
        val pressureMapped = mapPressure(
            smoothed.pressure,
            brush.dynamics.sizePressure
        )

        // 4. Apply opacity curve
        val opacityMapped = mapPressure(
            smoothed.pressure,
            brush.dynamics.opacityPressure
        )

        // 5. Calculate velocity-based size adjustment
        val velocity = calculateVelocity(smoothed, previousPoints.lastOrNull())
        val velocityAdjusted = if (brush.dynamics.velocitySensitivity > 0) {
            val velocityFactor = 1f - (velocity * brush.dynamics.velocitySensitivity).coerceIn(0f, 0.8f)
            smoothed.copy(pressure = pressureMapped * velocityFactor)
        } else {
            smoothed.copy(pressure = pressureMapped)
        }

        // 6. Apply tilt compensation
        val tiltCompensated = if (brush.dynamics.tiltSensitivity > 0) {
            applyTiltCompensation(velocityAdjusted, brush)
        } else {
            velocityAdjusted
        }

        state.stabilizerBuffer.clear()
        return listOf(tiltCompensated)
    }

    /**
     * Generate interpolated points between two stroke points
     * for smooth dab-based rendering.
     */
    fun interpolatePoints(
        from: StrokePoint,
        to: StrokePoint,
        spacing: Float,
        brushSize: Float
    ): List<StrokePoint> {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val distance = sqrt(dx * dx + dy * dy)

        if (distance < spacing * brushSize * 0.1f) {
            return listOf(to)
        }

        val stepSize = spacing * brushSize * 0.5f
        val steps = max(1, (distance / stepSize).toInt())

        return (1..steps).map { i ->
            val t = i.toFloat() / steps
            StrokePoint(
                x = from.x + dx * t,
                y = from.y + dy * t,
                pressure = from.pressure + (to.pressure - from.pressure) * t,
                tiltX = from.tiltX + (to.tiltX - from.tiltX) * t,
                tiltY = from.tiltY + (to.tiltY - from.tiltY) * t,
                orientation = from.orientation + (to.orientation - from.orientation) * t,
                timestamp = from.timestamp + ((to.timestamp - from.timestamp) * t).toLong()
            )
        }
    }

    /**
     * Apply Catmull-Rom smoothing to reduce jitter.
     */
    private fun applySmoothing(
        point: StrokePoint,
        previousPoints: List<StrokePoint>,
        smoothing: Float
    ): StrokePoint {
        if (smoothing <= 0f || previousPoints.isEmpty()) return point

        val windowSize = max(2, (smoothing * 10).toInt())
        val relevantPoints = previousPoints.takeLast(windowSize) + point

        if (relevantPoints.size < 3) {
            return averagePoints(relevantPoints)
        }

        // Weighted moving average
        val weights = relevantPoints.indices.map { i ->
            val t = i.toFloat() / (relevantPoints.size - 1)
            // Smooth bell curve weighting, centered on recent points
            t * t * (3 - 2 * t) // smoothstep
        }
        val totalWeight = weights.sum()

        var sumX = 0f
        var sumY = 0f
        var sumPressure = 0f

        for (i in relevantPoints.indices) {
            val w = weights[i] / totalWeight
            sumX += relevantPoints[i].x * w
            sumY += relevantPoints[i].y * w
            sumPressure += relevantPoints[i].pressure * w
        }

        return point.copy(
            x = sumX,
            y = sumY,
            pressure = sumPressure
        )
    }

    private fun averagePoints(points: List<StrokePoint>): StrokePoint {
        if (points.isEmpty()) return StrokePoint(0f, 0f)
        if (points.size == 1) return points[0]

        return StrokePoint(
            x = points.map { it.x }.average().toFloat(),
            y = points.map { it.y }.average().toFloat(),
            pressure = points.map { it.pressure }.average().toFloat(),
            tiltX = points.map { it.tiltX }.average().toFloat(),
            tiltY = points.map { it.tiltY }.average().toFloat(),
            orientation = points.map { it.orientation }.average().toFloat()
        )
    }

    /**
     * Map raw pressure through a pressure curve.
     */
    private fun mapPressure(pressure: Float, curve: PressureCurve): Float {
        return curve.evaluate(pressure.coerceIn(0f, 1f))
    }

    /**
     * Calculate velocity between two points.
     */
    private fun calculateVelocity(current: StrokePoint, previous: StrokePoint?): Float {
        if (previous == null) return 0f
        val dx = current.x - previous.x
        val dy = current.y - previous.y
        val dt = max(1L, current.timestamp - previous.timestamp)
        return sqrt(dx * dx + dy * dy) / dt * 1000f // pixels per second (normalized)
    }

    /**
     * Apply tilt-based size and shape adjustments.
     */
    private fun applyTiltCompensation(point: StrokePoint, brush: Brush): StrokePoint {
        val tiltMagnitude = sqrt(
            point.tiltX * point.tiltX + point.tiltY * point.tiltY
        )
        val tiltFactor = 1f - tiltMagnitude * brush.dynamics.tiltSensitivity * 0.3f
        return point.copy(
            pressure = point.pressure * tiltFactor.coerceIn(0.3f, 1f)
        )
    }
}

/**
 * Mutable state held by the brush engine between processing calls.
 */
class BrushEngineState {
    val stabilizerBuffer: LinkedList<StrokePoint> = LinkedList()
    var lastVelocity: Float = 0f
    var lastSmoothedPoint: StrokePoint? = null

    fun reset() {
        stabilizerBuffer.clear()
        lastVelocity = 0f
        lastSmoothedPoint = null
    }
}
