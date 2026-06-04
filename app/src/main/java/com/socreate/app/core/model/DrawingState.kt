package com.socreate.app.core.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import java.util.UUID

/**
 * Complete drawing state for MVI architecture
 */
data class DrawingState(
    val canvasWidth: Int = 2800,
    val canvasHeight: Int = 1752,
    val canvasBackground: Color = Color.White,
    
    val brush: BrushState = BrushState(),
    val symmetry: SymmetryMode = SymmetryMode.NONE,
    val shapeDetection: Boolean = false,
    
    val layers: List<LayerState> = emptyList(),
    val activeLayerId: String? = null,
    
    val timeline: TimelineState = TimelineState(),
    val onionSkin: OnionSkinState = OnionSkinState(),
    
    val undoStack: List<StrokeData> = emptyList(),
    val redoStack: List<StrokeData> = emptyList(),
    val currentStroke: StrokeData? = null,
    
    val isDrawing: Boolean = false,
    val controlMode: Boolean = false,
    val keyframeMode: Boolean = false,
    
    val panels: PanelState = PanelState(),
    
    val settings: AppSettings = AppSettings(),
    
    val gallery: List<CanvasMetadata> = emptyList()
) {
    companion object {
        fun initial(): DrawingState {
            val bgLayer = LayerState(
                id = UUID.randomUUID().toString(),
                name = "Background",
                color = Color(0xFFFF6B6B),
                isVisible = true
            )
            return DrawingState(
                layers = listOf(bgLayer),
                activeLayerId = bgLayer.id
            )
        }
    }
}

/**
 * Brush configuration state
 */
data class BrushState(
    val type: BrushType = BrushType.PENCIL,
    val size: Float = 12f,
    val opacity: Float = 100f,
    val hardness: Float = 100f,
    val color: Color = Color.Black,
    val pressureEnabled: Boolean = true,
    val pressureCurve: PressureCurve = PressureCurve.LINEAR,
    val recentColors: List<Color> = listOf(
        Color.Black, Color.White,
        Color(0xFFFF6B35), Color(0xFF4ECDC4),
        Color(0xFF9B59B6), Color(0xFFE74C3C),
        Color(0xFF2ECC71), Color(0xFF3498DB)
    )
)

enum class BrushType {
    PENCIL, PEN, MARKER, BRUSH, AIRBRUSH, ERASER, SMUDGE, BLUR, SHARPEN, CLONE, HEAL, DODGE, BURN
}

enum class PressureCurve {
    LINEAR, EASE_IN, EASE_OUT, S_CURVE, CUSTOM
}

/**
 * Symmetry modes for drawing
 */
enum class SymmetryMode {
    NONE,
    HORIZONTAL,
    VERTICAL,
    QUADRANT,
    RADIAL_4,
    RADIAL_6,
    RADIAL_8,
    MANDALA
}

/**
 * Layer state
 */
data class LayerState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Layer",
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 100f,
    val blendMode: BlendMode = BlendMode.SrcOver,
    val color: Color = Color.Gray,
    val thumbnailSize: ThumbnailSize = ThumbnailSize.MEDIUM,
    val thumbnailScope: ThumbnailScope = ThumbnailScope.LAYER_CONTENTS
)

enum class ThumbnailSize { SMALL, MEDIUM, LARGE }
enum class ThumbnailScope { WHOLE_CANVAS, LAYER_CONTENTS }

/**
 * Stroke data for rendering
 */
data class StrokeData(
    val id: String = UUID.randomUUID().toString(),
    val points: List<Offset> = emptyList(),
    val brushType: BrushType = BrushType.PENCIL,
    val color: Color = Color.Black,
    val size: Float = 12f,
    val opacity: Float = 100f,
    val pressurePoints: List<Float> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Timeline state
 */
data class TimelineState(
    val frames: List<FrameData> = emptyList(),
    val currentFrameIndex: Int = 0,
    val fps: Int = 12,
    val maxDurationSeconds: Int = 180,
    val isPlaying: Boolean = false,
    val loopMode: LoopMode = LoopMode.REPEAT
) {
    val maxFrames: Int get() = fps * maxDurationSeconds
}

data class FrameData(
    val id: String = UUID.randomUUID().toString(),
    val frameNumber: Int = 0,
    val isKeyframe: Boolean = false,
    val layerStates: Map<String, LayerFrameState> = emptyMap()
)

data class LayerFrameState(
    val visible: Boolean = true,
    val opacity: Float = 100f
)

enum class LoopMode { NONE, REPEAT, REVERSE, PING_PONG }

/**
 * Onion skinning state
 */
data class OnionSkinState(
    val isEnabled: Boolean = true,
    val previousFrames: Int = 3,
    val nextFrames: Int = 0,
    val opacity: Float = 30f,
    val tintPrevious: Color = Color(0xFFFF6B6B),
    val tintNext: Color = Color(0xFF4ECDC4)
)

/**
 * Panel visibility state
 */
data class PanelState(
    val brushes: PanelVisibility = PanelVisibility(),
    val layers: PanelVisibility = PanelVisibility(),
    val timeline: PanelVisibility = PanelVisibility(),
    val gallery: PanelVisibility = PanelVisibility(),
    val settings: PanelVisibility = PanelVisibility(),
    val onionSkin: PanelVisibility = PanelVisibility(),
    val symmetry: PanelVisibility = PanelVisibility()
)

data class PanelVisibility(
    val isOpen: Boolean = false,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val isDragging: Boolean = false
)

/**
 * App settings
 */
data class AppSettings(
    val palmRejection: Boolean = true,
    val stylusOnly: Boolean = false,
    val lowLatency: Boolean = true,
    val gestureUndo: Boolean = true,
    val gestureRedo: Boolean = true,
    val gestureFullscreen: Boolean = false,
    val autoSave: Boolean = true
)

/**
 * Canvas metadata for gallery
 */
data class CanvasMetadata(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled",
    val width: Int,
    val height: Int,
    val fps: Int,
    val thumbnail: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val layerCount: Int = 1,
    val frameCount: Int = 1
)
