package com.socreate.app.ui.drawing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.socreate.app.core.model.*

/**
 * Main drawing canvas - fills entire screen
 * Handles S Pen pressure, tilt, palm rejection, and symmetry
 */
@Composable
fun DrawingCanvas(
    modifier: Modifier = Modifier,
    state: DrawingState,
    onStrokeStart: (Offset) -> Unit = {},
    onStrokeMove: (Offset, Float) -> Unit = { _, _ -> },
    onStrokeEnd: () -> Unit = {}
) {
    val layers = state.layers
    val activeLayerId = state.activeLayerId
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInteropFilter { event ->
                // Palm rejection
                if (state.settings.palmRejection) {
                    if (event.toolType == android.view.MotionEvent.TOOL_TYPE_FINGER && 
                        event.size > 20f) {
                        return@pointerInteropFilter true
                    }
                }
                
                // Stylus only mode
                if (state.settings.stylusOnly && 
                    event.toolType != android.view.MotionEvent.TOOL_TYPE_STYLUS) {
                    return@pointerInteropFilter true
                }
                
                false
            }
            .pointerInput(state.brush) {
                detectDragGestures(
                    onDragStart = { offset ->
                        onStrokeStart(offset)
                    },
                    onDrag = { change, dragAmount ->
                        val pressure = change.pressure
                        onStrokeMove(change.position, pressure)
                    },
                    onDragEnd = {
                        onStrokeEnd()
                    },
                    onDragCancel = {
                        onStrokeEnd()
                    }
                )
            }
    ) {
        // Draw onion skin layers first (behind current)
        drawOnionSkin(state)
        
        // Draw all visible layers
        layers.forEach { layer ->
            if (layer.isVisible) {
                saveLayer(size.toRect(), Paint().apply { alpha = layer.opacity / 100f })
                restore()
            }
        }
        
        // Draw symmetry guides
        drawSymmetryGuides(state)
    }
}

@Composable
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOnionSkin(state: DrawingState) {
    if (!state.onionSkin.isEnabled) return
    
    // Previous frames in red tint
    val prevAlpha = state.onionSkin.opacity / 100f
    repeat(state.onionSkin.previousFrames) { i ->
        saveLayer(size.toRect(), Paint().apply {
            alpha = prevAlpha * (1f - (i + 1f) / (state.onionSkin.previousFrames + 1))
            colorFilter = ColorFilter.tint(state.onionSkin.tintPrevious.copy(alpha = 0.5f))
        })
        restore()
    }
    
    // Next frames in blue tint
    repeat(state.onionSkin.nextFrames) { i ->
        saveLayer(size.toRect(), Paint().apply {
            alpha = prevAlpha * (1f - (i + 1f) / (state.onionSkin.nextFrames + 1))
            colorFilter = ColorFilter.tint(state.onionSkin.tintNext.copy(alpha = 0.5f))
        })
        restore()
    }
}

@Composable
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSymmetryGuides(state: DrawingState) {
    if (state.symmetry == SymmetryMode.NONE) return
    
    val cx = size.width / 2f
    val cy = size.height / 2f
    
    drawLine(
        color = Color(0xFF9B59B6).copy(alpha = 0.3f),
        start = Offset(cx, 0f),
        end = Offset(cx, size.height),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
    )
    
    if (state.symmetry == SymmetryMode.VERTICAL || state.symmetry == SymmetryMode.QUADRANT) {
        drawLine(
            color = Color(0xFF9B59B6).copy(alpha = 0.3f),
            start = Offset(0f, cy),
            end = Offset(size.width, cy),
            strokeWidth = 1f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
        )
    }
    
    if (state.symmetry.name.startsWith("RADIAL") || state.symmetry == SymmetryMode.MANDALA) {
        val segments = when (state.symmetry) {
            SymmetryMode.RADIAL_4 -> 4
            SymmetryMode.RADIAL_6 -> 6
            SymmetryMode.RADIAL_8 -> 8
            SymmetryMode.MANDALA -> 12
            else -> 4
        }
        
        repeat(segments) { i ->
            val angle = (i * 360f / segments) * Math.PI.toFloat() / 180f
            drawLine(
                color = Color(0xFF9B59B6).copy(alpha = 0.3f),
                start = Offset(cx, cy),
                end = Offset(
                    cx + kotlin.math.cos(angle.toDouble()).toFloat() * size.width,
                    cy + kotlin.math.sin(angle.toDouble()).toFloat() * size.height
                ),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )
        }
    }
}

/**
 * Stroke path with symmetry support
 */
@Composable
fun drawSymmetricStroke(
    drawScope: androidx.compose.ui.graphics.drawscope.DrawScope,
    points: List<Offset>,
    symmetryMode: SymmetryMode,
    brush: BrushState
) {
    if (points.size < 2) return
    
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            lineTo(points[i].x, points[i].y)
        }
    }
    
    drawScope.drawPath(
        path = path,
        color = brush.color,
        style = androidx.compose.ui.graphics.drawscope.Stroke(
            width = brush.size,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        ),
        alpha = brush.opacity / 100f
    )
    
    // Draw mirrored strokes
    val cx = drawScope.size.width / 2f
    val cy = drawScope.size.height / 2f
    
    when (symmetryMode) {
        SymmetryMode.HORIZONTAL -> {
            val mirroredPath = Path().apply {
                points.forEach { p ->
                    if (this == path) moveTo(2 * cx - p.x, p.y)
                    else lineTo(2 * cx - p.x, p.y)
                }
            }
            drawScope.drawPath(
                path = mirroredPath,
                color = brush.color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = brush.size,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = brush.opacity / 100f
            )
        }
        SymmetryMode.VERTICAL -> {
            val mirroredPath = Path().apply {
                points.forEach { p ->
                    if (this == path) moveTo(p.x, 2 * cy - p.y)
                    else lineTo(p.x, 2 * cy - p.y)
                }
            }
            drawScope.drawPath(
                path = mirroredPath,
                color = brush.color,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = brush.size,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                ),
                alpha = brush.opacity / 100f
            )
        }
        SymmetryMode.QUADRANT -> {
            // Mirror both axes
            val hMirrored = Path().apply {
                points.forEach { p ->
                    if (this == path) moveTo(2 * cx - p.x, p.y)
                    else lineTo(2 * cx - p.x, p.y)
                }
            }
            val vMirrored = Path().apply {
                points.forEach { p ->
                    if (this == path) moveTo(p.x, 2 * cy - p.y)
                    else lineTo(p.x, 2 * cy - p.y)
                }
            }
            val hvMirrored = Path().apply {
                points.forEach { p ->
                    if (this == path) moveTo(2 * cx - p.x, 2 * cy - p.y)
                    else lineTo(2 * cx - p.x, 2 * cy - p.y)
                }
            }
            
            listOf(hMirrored, vMirrored, hvMirrored).forEach { p ->
                drawScope.drawPath(
                    path = p,
                    color = brush.color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = brush.size,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    ),
                    alpha = brush.opacity / 100f
                )
            }
        }
        else -> {
            // Radial modes
            val segments = when (symmetryMode) {
                SymmetryMode.RADIAL_4 -> 4
                SymmetryMode.RADIAL_6 -> 6
                SymmetryMode.RADIAL_8 -> 8
                SymmetryMode.MANDALA -> 12
                else -> 0
            }
            
            if (segments > 0) {
                repeat(segments - 1) { i ->
                    val angle = ((i + 1) * 360f / segments) * Math.PI.toFloat() / 180f
                    val rotatedPath = Path().apply {
                        points.forEach { p ->
                            val dx = p.x - cx
                            val dy = p.y - cy
                            val nx = cx + dx * kotlin.math.cos(angle.toDouble()).toFloat() - 
                                     dy * kotlin.math.sin(angle.toDouble()).toFloat()
                            val ny = cy + dx * kotlin.math.sin(angle.toDouble()).toFloat() + 
                                     dy * kotlin.math.cos(angle.toDouble()).toFloat()
                            if (this == path) moveTo(nx, ny)
                            else lineTo(nx, ny)
                        }
                    }
                    drawScope.drawPath(
                        path = rotatedPath,
                        color = brush.color,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = brush.size,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        ),
                        alpha = brush.opacity / 100f
                    )
                }
            }
        }
    }
}
