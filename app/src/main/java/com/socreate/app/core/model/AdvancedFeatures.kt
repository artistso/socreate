package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Features inspired by FlipaClip, ibisPaint, Clip Studio Paint, and HiPaint.
 *
 * This file contains the domain models for:
 * - Symmetry Drawing (HiPaint)
 * - Reference Images (HiPaint)
 * - Audio Tracks (FlipaClip)
 * - Animation Frame Management (FlipaClip)
 * - Time-lapse Recording (ibisPaint / HiPaint)
 * - Smart Shapes (Clip Studio v5)
 * - Auto-Save / Crash Recovery (Clip Studio v5)
 * - Custom Brush Patterns (ibisPaint)
 * - Gesture Shortcuts (HiPaint)
 * - Numeric Input (ibisPaint)
 * - Drawing Outside Canvas (FlipaClip)
 */

// ─── Symmetry Drawing (from HiPaint) ────────────────────────────────────────

@Serializable
data class SymmetryConfig(
    val isEnabled: Boolean = false,
    val type: SymmetryType = SymmetryType.VERTICAL,
    val guideColor: SoCreateColor = SoCreateColor(0.5f, 0.5f, 1f, 0.4f),
    val guidePosition: Float = 0.5f,     // 0..1 normalized
    val rotationalCount: Int = 4,        // For rotational symmetry
    val showGuides: Boolean = true,
    val snapToGuide: Boolean = true
)

@Serializable
enum class SymmetryType(val displayName: String) {
    VERTICAL("Vertical"),
    HORIZONTAL("Horizontal"),
    QUADRANT("Quadrant (4-way)"),
    RADIAL("Radial"),
    ROTATIONAL("Rotational"),
    MANDALA("Mandala"),
    CUSTOM_ANGLE("Custom Angle")
}

// ─── Reference Images (from HiPaint) ────────────────────────────────────────

@Serializable
data class ReferenceImage(
    val id: String = UUID.randomUUID().toString(),
    val filePath: String,
    val name: String = "Reference",
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scale: Float = 1f,
    val opacity: Float = 0.3f,
    val rotation: Float = 0f,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false
)

// ─── Audio System (from FlipaClip) ──────────────────────────────────────────

@Serializable
data class AudioTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Audio",
    val filePath: String,
    val startFrame: Int = 0,
    val volume: Float = 1f,               // 0..1
    val isMuted: Boolean = false,
    val trimStartMs: Long = 0,            // Trim start in ms
    val trimEndMs: Long = 0,              // Trim end (0 = full)
    val waveformData: List<Float> = emptyList(),  // Simplified waveform for display
    val frameRate: Int = 24               // For frame-aligned editing
) {
    val durationMs: Long
        get() = if (trimEndMs > 0) trimEndMs - trimStartMs else 0L

    val startMs: Long
        get() = (startFrame * 1000L) / frameRate
}

// ─── Animation Frame Management (from FlipaClip Beta) ───────────────────────

@Serializable
data class AnimationFrame(
    val id: String = UUID.randomUUID().toString(),
    val layerId: String,
    val sortOrder: Int = 0,
    val duration: Float = 1f,             // In frames (1.0 = standard, 0.5 = half, 2.0 = double)
    val isKeyframe: Boolean = false,       // Keyframe vs in-between
    val label: String = "",               // Optional label (e.g., "mouth open", "walk cycle")
    val thumbnailPath: String? = null
) {
    /** Effective duration in frames (respects variable timing) */
    val effectiveDuration: Float get() = duration.coerceIn(0.1f, 60f)
}

// ─── Smart Shapes (from Clip Studio Paint v5) ───────────────────────────────

@Serializable
data class SmartShape(
    val type: SmartShapeType = SmartShapeType.FREEHAND,
    val snapThreshold: Float = 10f,       // Pixels before snapping kicks in
    val preserveBrushTexture: Boolean = true,
    val smoothing: Float = 0.5f,
    val fillAfterClose: Boolean = false,
    val fillColor: SoCreateColor = SoCreateColor.TRANSPARENT,
    val strokeColor: SoCreateColor = SoCreateColor.BLACK,
    val strokeWidth: Float = 2f
)

@Serializable
enum class SmartShapeType {
    FREEHAND,          // Detects intent from freehand strokes
    LINE,              // Perfect straight lines
    RECTANGLE,         // Rectangles with optional rounded corners
    ELLIPSE,           // Circles and ellipses
    BEZIER_CURVE,      // Bézier curves with control handles
    POLYGON,           // Regular polygons
    STAR,              // Stars
    ARROW              // Arrows
}

// ─── Puppet Warp (from Clip Studio Paint) ───────────────────────────────────

@Serializable
data class PuppetWarp(
    val id: String = UUID.randomUUID().toString(),
    val pins: List<WarpPin> = emptyList(),
    val meshResolution: Int = 10,         // Grid divisions
    val meshBounds: Bounds = Bounds.ZERO
)

@Serializable
data class WarpPin(
    val id: String = UUID.randomUUID().toString(),
    val originalX: Float,
    val originalY: Float,
    val currentX: Float,
    val currentY: Float,
    val isFixed: Boolean = false,         // Pinned in place
    val stiffness: Float = 1f            // 0..1 how rigid the pin is
)

// ─── Auto-Save / Crash Recovery (from Clip Studio v5) ───────────────────────

@Serializable
data class AutoSaveConfig(
    val isEnabled: Boolean = true,
    val intervalSeconds: Int = 30,        // Save every 30 seconds
    val maxBackups: Int = 5,              // Keep last 5 backups
    val backupDirectory: String = "autosave"
)

@Serializable
data class RecoverySnapshot(
    val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val canvasWidth: Int,
    val canvasHeight: Int,
    val layerCount: Int,
    val thumbnailPath: String? = null,
    val wasCrashRecovery: Boolean = false
)

// ─── Custom Brush Patterns (from ibisPaint v13) ─────────────────────────────

@Serializable
data class BrushPattern(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Custom Pattern",
    val shapeBitmapPath: String? = null,  // Path to shape texture image
    val grainBitmapPath: String? = null,  // Path to grain texture image
    val shapeMode: PatternShapeMode = PatternShapeMode.STAMP,
    val grainMode: PatternGrainMode = PatternGrainMode.TEXTURE,
    val author: String = "",
    val isImported: Boolean = false
)

@Serializable
enum class PatternShapeMode {
    STAMP,               // Single stamp per dab
    SCATTER,             // Multiple scattered stamps
    DUAL_STAMP,          // Two overlapping stamps
    BRISTLE              // Bristle-like arrangement
}

@Serializable
enum class PatternGrainMode {
    TEXTURE,             // Applied as texture overlay
    CLIPPING,           // Clips the shape
    PATTERN_FILL,        // Tile-based pattern
    NOISE               // Procedural noise
}

// ─── Gesture Shortcuts (from HiPaint) ───────────────────────────────────────

@Serializable
data class GestureConfig(
    val twoFingerUndo: Boolean = true,           // Two-finger tap = undo
    val twoFingerTapCount: Int = 1,              // Number of taps needed
    val threeFingerRedo: Boolean = true,         // Three-finger tap = redo
    val longPressEyedropper: Boolean = true,     // Long press = color picker
    val longPressDurationMs: Long = 300L,        // How long to hold
    val quickMenuOnSpenButton: Boolean = true,   // S Pen button = quick menu
    val pinchZoomEnabled: Boolean = true,
    val quickRotateEnabled: Boolean = true,      // Two-finger twist = rotate
    val disableFingerDrawing: Boolean = false    // Palm rejection strict mode
)

// ─── Numeric Input (from ibisPaint v13) ─────────────────────────────────────

@Serializable
data class NumericInput(
    val value: Float = 0f,
    val min: Float = 0f,
    val max: Float = 9999f,
    val step: Float = 1f,
    val unit: NumericInputUnit = NumericInputUnit.PIXELS,
    val decimalPlaces: Int = 1
)

@Serializable
enum class NumericInputUnit(val suffix: String) {
    PIXELS("px"),
    PERCENT("%"),
    DEGREES("°"),
    SECONDS("s"),
    FRAMES("f"),
    DPI("dpi"),
    NONE("")
}

// ─── Time-lapse Recording (from ibisPaint / HiPaint) ────────────────────────

@Serializable
data class TimelapseConfig(
    val isRecording: Boolean = false,
    val includeUndoRedo: Boolean = false,       // Show undos in replay
    val playbackSpeed: Float = 8f,              // Speed multiplier for export
    val resolution: TimelapseResolution = TimelapseResolution.FULL,
    val maxDurationMinutes: Int = 30,
    val format: TimelapseFormat = TimelapseFormat.MP4
)

@Serializable
enum class TimelapseResolution {
    HALF,       // Half canvas resolution
    FULL,       // Full canvas resolution
    OUTPUT      // Match export resolution
}

@Serializable
enum class TimelapseFormat {
    MP4, GIF
}

// ─── Draw Outside Canvas (from FlipaClip Beta) ──────────────────────────────

@Serializable
data class ExtendedCanvas(
    val visibleWidth: Int = 2800,           // What the viewer sees
    val visibleHeight: Int = 1752,
    val extendedWidth: Int = 5600,          // Full drawing area (2x visible)
    val extendedHeight: Int = 3504,
    val bgColor: SoCreateColor = SoCreateColor(0.95f, 0.95f, 0.95f),
    val showBounds: Boolean = true,          // Show visible area boundary
    val boundsColor: SoCreateColor = SoCreateColor(0.8f, 0.2f, 0.2f, 0.3f)
) {
    val horizontalPadding: Int get() = (extendedWidth - visibleWidth) / 2
    val verticalPadding: Int get() = (extendedHeight - visibleHeight) / 2
}

// ─── Velocity Brush Setting (from Clip Studio v5) ───────────────────────────

@Serializable
data class VelocitySettings(
    val isEnabled: Boolean = false,
    val sizeVelocityCurve: PressureCurve = PressureCurve(),  // Reuse pressure curve type
    val opacityVelocityCurve: PressureCurve = PressureCurve(),
    val velocitySmoothing: Float = 0.5f,
    val minSpeed: Float = 0f,               // Pixels/second
    val maxSpeed: Float = 2000f              // Pixels/second
)

// ─── Liquify (from Clip Studio Paint) ───────────────────────────────────────

@Serializable
enum class LiquifyMode {
    PUSH,         // Push pixels in stroke direction
    TWIRL_CW,     // Twirl clockwise
    TWIRL_CCW,    // Twirl counter-clockwise
    PINCH,        // Pinch toward center
    BLOAT,        // Bloat outward from center
    RECONSTRUCT   // Revert to original
}

@Serializable
data class LiquifySettings(
    val mode: LiquifyMode = LiquifyMode.PUSH,
    val brushSize: Float = 60f,
    val pressure: Float = 0.5f,            // Effect strength 0..1
    val turbulence: Float = 0f             // Jitter amount 0..1
)

// ─── Surrounding Fill / Surrounding Eraser (from ibisPaint v13) ─────────────

@Serializable
enum class FillMode {
    FLOOD_FILL,           // Standard flood fill
    SURROUNDING_FILL,     // Fill everything EXCEPT the enclosed area
    LASSO_FILL,           // Fill within lasso selection
    GRADIENT_FILL,        // Fill with gradient
    PATTERN_FILL          // Fill with pattern/texture
}

@Serializable
data class FillSettings(
    val mode: FillMode = FillMode.FLOOD_FILL,
    val tolerance: Float = 0.1f,           // 0..1 color tolerance
    val antiAlias: Boolean = true,
    val fillToLayer: String? = null,       // Target layer ID (null = current)
    val referenceAllLayers: Boolean = false,
    val growAmount: Int = 0,               // Expand selection by N px
    val featherAmount: Float = 0f          // Soften edges
)

// ─── Shading Assist (from Clip Studio Paint v5) ─────────────────────────────

@Serializable
data class ShadingAssistConfig(
    val isEnabled: Boolean = false,
    val lightAngle: Float = 135f,          // Degrees (top-left = 135°)
    val shadowColor: SoCreateColor = SoCreateColor.BLACK.withAlpha(0.3f),
    val highlightColor: SoCreateColor = SoCreateColor.WHITE.withAlpha(0.2f),
    val shadowIntensity: Float = 0.5f,     // 0..1
    val highlightIntensity: Float = 0.3f,  // 0..1
    val ambientOcclusion: Boolean = false,
    val targetLayer: String? = null        // Auto-create shading layer
)

// ─── Color Match (from Clip Studio Paint v5) ────────────────────────────────

@Serializable
data class ColorMatchConfig(
    val referenceImagePath: String? = null,
    val sourceColors: List<SoCreateColor> = emptyList(),
    val targetColors: List<SoCreateColor> = emptyList(),
    val matchStrength: Float = 0.8f,       // 0..1
    val preserveLuminosity: Boolean = true
)

// ─── Floating Windows / Panels (from ibisPaint v14) ─────────────────────────

@Serializable
data class FloatingPanel(
    val id: String = UUID.randomUUID().toString(),
    val type: FloatingPanelType,
    val x: Float = 0f,                     // Position on screen
    val y: Float = 0f,
    val width: Float = 200f,
    val height: Float = 300f,
    val isVisible: Boolean = true,
    val isCollapsed: Boolean = false,
    val opacity: Float = 0.9f
)

@Serializable
enum class FloatingPanelType {
    COLOR_WHEEL,
    BRUSH_SETTINGS,
    LAYER_LIST,
    COLOR_PALETTE,
    REFERENCE_IMAGE,
    NAVIGATOR,
    BRUSH_PREVIEW
}
