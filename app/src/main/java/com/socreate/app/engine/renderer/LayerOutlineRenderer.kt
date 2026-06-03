package com.socreate.app.engine.renderer

import com.socreate.app.core.model.*

/**
 * Layer Outline Renderer (Aseprite/Resprite-style).
 *
 * Detects and renders outlines around layer content for:
 * - Quick visual identification of layer boundaries
 * - Animation alignment reference
 * - Multi-layer composition review
 * - Selection assistance
 *
 * Outline types:
 * - Solid lines (default)
 * - Dashed lines
 * - Dotted lines
 * - Marching ants (animated dashed lines)
 *
 * Each layer can have a unique outline color:
 * - Active layer: Green (configurable)
 * - Locked layers: Red (configurable)
 * - Other visible layers: Cyan (configurable)
 * - Hidden layers: Optional (off by default)
 *
 * Uses alpha threshold edge detection to find content boundaries.
 */
class LayerOutlineRenderer {

    /**
     * Determine the outline color for a given layer based on its state.
     */
    fun getOutlineColorForLayer(
        layer: Layer,
        activeLayerId: String?,
        config: LayerOutlineConfig
    ): SoCreateColor? {
        if (!config.isEnabled) return null

        // Check if this layer type should show outline
        when (config.mode) {
            LayerOutlineMode.ACTIVE_ONLY -> {
                if (layer.id != activeLayerId) return null
            }
            LayerOutlineMode.ALL_VISIBLE -> {
                if (!layer.isVisible) {
                    if (!config.showForHidden) return null
                }
            }
            LayerOutlineMode.NON_EMPTY -> {
                // Would check bitmap content; simplified for model layer
                if (!layer.isVisible && !config.showForHidden) return null
            }
            LayerOutlineMode.ANIMATION_FRAMES -> {
                if (layer.type != LayerType.ANIMATION_FLIPBOOK &&
                    layer.type != LayerType.ANIMATION_KEYFRAME) return null
            }
            LayerOutlineMode.SELECTED_LAYERS, LayerOutlineMode.CUSTOM -> {
                // Handled by selection state
            }
        }

        // Locked layers
        if (layer.isLocked && !config.showForLocked) return null

        // Pick color based on state
        return when {
            layer.id == activeLayerId -> config.activeLayerColor
            layer.isLocked -> config.lockedLayerColor
            else -> config.color
        }
    }

    /**
     * Generate the dash pattern intervals for the outline path effect.
     */
    fun getDashIntervals(pattern: OutlineDashPattern): FloatArray {
        return when (pattern) {
            OutlineDashPattern.SOLID -> floatArrayOf()  // No dash — solid line
            OutlineDashPattern.DASHED -> floatArrayOf(10f, 6f)
            OutlineDashPattern.DOTTED -> floatArrayOf(2f, 4f)
            OutlineDashPattern.DASH_DOT -> floatArrayOf(10f, 4f, 2f, 4f)
            OutlineDashPattern.MARCHING_ANTS -> floatArrayOf(6f, 6f)
        }
    }

    /**
     * Calculate the marching ants animation offset.
     * @param timeNanos Current time in nanoseconds
     * @param speed Animation speed (0..2, default 1)
     * @return Phase offset for dash pattern
     */
    fun getMarchingAntsOffset(timeNanos: Long, speed: Float = 1f): Float {
        val periodNanos = 1_000_000_000L  // 1 second per cycle
        return ((timeNanos * speed) % periodNanos).toFloat() / periodNanos * 12f
    }

    /**
     * Perform edge detection on a bitmap's alpha channel.
     * Returns a list of outline points for the layer content.
     *
     * Uses Sobel edge detection on the alpha channel with the configured
     * detection threshold.
     *
     * @param alphaData Raw alpha channel data (0..255)
     * @param width Bitmap width
     * @param height Bitmap height
     * @param threshold Alpha threshold for edge detection (0..1)
     * @return List of edge points in normalized coordinates (0..1)
     */
    fun detectEdges(
        alphaData: IntArray,
        width: Int,
        height: Int,
        threshold: Float
    ): List<Pair<Float, Float>> {
        val edges = mutableListOf<Pair<Float, Float>>()
        val thresholdByte = (threshold * 255).toInt()

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val idx = y * width + x
                val alpha = alphaData[idx]

                if (alpha < thresholdByte) continue

                // Check if any neighbor is below threshold (edge pixel)
                val isEdge = (
                    alphaData[idx - 1] < thresholdByte ||
                    alphaData[idx + 1] < thresholdByte ||
                    alphaData[idx - width] < thresholdByte ||
                    alphaData[idx + width] < thresholdByte ||
                    alphaData[idx - width - 1] < thresholdByte ||
                    alphaData[idx - width + 1] < thresholdByte ||
                    alphaData[idx + width - 1] < thresholdByte ||
                    alphaData[idx + width + 1] < thresholdByte
                )

                if (isEdge) {
                    edges.add(x.toFloat() / width to y.toFloat() / height)
                }
            }
        }

        return edges
    }

    /**
     * Simplify edge points into outline paths for rendering.
     * Reduces point count by connecting nearby edge points into paths.
     */
    fun simplifyEdges(
        edgePoints: List<Pair<Float, Float>>,
        simplification: Float = 2f
    ): List<List<Pair<Float, Float>>> {
        if (edgePoints.isEmpty()) return emptyList()

        // Grid-based clustering
        val gridSize = simplification / 1000f
        val paths = mutableListOf<MutableList<Pair<Float, Float>>>()
        val visited = mutableSetOf<Int>()

        for (i in edgePoints.indices) {
            if (i in visited) continue

            val path = mutableListOf<Pair<Float, Float>>()
            val queue = ArrayDeque<Int>()
            queue.add(i)
            visited.add(i)

            while (queue.isNotEmpty()) {
                val currentIdx = queue.removeFirst()
                val (cx, cy) = edgePoints[currentIdx]
                path.add(cx to cy)

                for (j in edgePoints.indices) {
                    if (j in visited) continue
                    val (ex, ey) = edgePoints[j]
                    val dist = kotlin.math.sqrt((ex - cx) * (ex - cx) + (ey - cy) * (ey - cy))
                    if (dist < gridSize * 3) {
                        visited.add(j)
                        queue.add(j)
                    }
                }
            }

            if (path.size >= 3) {
                paths.add(path)
            }
        }

        return paths
    }
}
