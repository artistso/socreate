package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single stroke (path) drawn on the canvas.
 * Contains all input points with pressure, tilt, and timing data
 * for accurate rendering and potential re-rendering.
 */
@Serializable
data class Stroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<StrokePoint> = emptyList(),
    val brushId: String = Brush.HB_PENCIL_ID,
    val brushProperties: BrushProperties = BrushProperties(),
    val color: SoCreateColor = SoCreateColor.BLACK,
    val blendMode: BlendMode = BlendMode.NORMAL,
    val layerId: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val isEmpty: Boolean get() = points.isEmpty()
    val pointCount: Int get() = points.size

    val bounds: Bounds
        get() {
            if (points.isEmpty()) return Bounds.ZERO
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            val halfSize = brushProperties.baseSize / 2f
            for (p in points) {
                minX = minOf(minX, p.x - halfSize)
                minY = minOf(minY, p.y - halfSize)
                maxX = maxOf(maxX, p.x + halfSize)
                maxY = maxOf(maxY, p.y + halfSize)
            }
            return Bounds(minX, minY, maxX - minX, maxY - minY)
        }

    fun addPoint(point: StrokePoint): Stroke {
        return copy(points = points + point)
    }

    fun withPoints(newPoints: List<StrokePoint>): Stroke {
        return copy(points = newPoints)
    }
}

/**
 * A single input point during a stroke, capturing full stylus data.
 */
@Serializable
data class StrokePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,       // 0..1
    val tiltX: Float = 0f,          // radians
    val tiltY: Float = 0f,          // radians
    val orientation: Float = 0f,    // azimuth in radians
    val timestamp: Long = 0L,       // nanoseconds since stroke start
    val isMajorTouchEvent: Boolean = true, // true = stylus, false = finger/eraser
    val toolType: ToolType = ToolType.STYLUS
) {
    companion object {
        fun simple(x: Float, y: Float) = StrokePoint(x, y)
        fun withPressure(x: Float, y: Float, pressure: Float) = StrokePoint(x, y, pressure)
    }
}

@Serializable
enum class ToolType {
    STYLUS, FINGER, ERASER, MOUSE, UNKNOWN
}

/**
 * Axis-aligned bounding box for spatial operations.
 */
@Serializable
data class Bounds(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 0f,
    val height: Float = 0f
) {
    val left get() = x
    val top get() = y
    val right get() = x + width
    val bottom get() = y + height
    val centerX get() = x + width / 2
    val centerY get() = y + height / 2
    val isEmpty get() = width <= 0 || height <= 0

    fun intersects(other: Bounds): Boolean {
        return !(right < other.left || left > other.right ||
                bottom < other.top || top > other.bottom)
    }

    fun contains(px: Float, py: Float): Boolean {
        return px in left..right && py in top..bottom
    }

    fun inflate(amount: Float) = Bounds(
        x - amount, y - amount,
        width + amount * 2, height + amount * 2
    )

    fun union(other: Bounds): Bounds {
        val newX = minOf(left, other.left)
        val newY = minOf(top, other.top)
        return Bounds(
            newX, newY,
            maxOf(right, other.right) - newX,
            maxOf(bottom, other.bottom) - newY
        )
    }

    companion object {
        val ZERO = Bounds(0f, 0f, 0f, 0f)
    }
}

/**
 * Canvas state containing all strokes for a single frame/layer.
 */
@Serializable
data class StrokeCollection(
    val strokes: List<Stroke> = emptyList()
) {
    val isEmpty: Boolean get() = strokes.isEmpty()
    val count: Int get() = strokes.size

    fun addStroke(stroke: Stroke) = copy(strokes = strokes + stroke)

    fun removeStroke(strokeId: String) =
        copy(strokes = strokes.filter { it.id != strokeId })

    fun getStrokesInBounds(bounds: Bounds): List<Stroke> =
        strokes.filter { it.bounds.intersects(bounds) }
}
