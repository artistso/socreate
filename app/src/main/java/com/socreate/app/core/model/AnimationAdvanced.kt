package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Enhanced animation features inspired by Aseprite and Resprite.
 *
 * Includes:
 * - Advanced onion skin with blend modes and effects
 * - Layer outlining (Aseprite-style)
 * - Multi-frame selection with batch editing
 * - On-screen modifier keys (Ctrl/Cmd/Shift)
 * - Frame tagging and color coding
 * - Ghost frames and motion trails
 */

// ─── Enhanced Onion Skin ────────────────────────────────────────────────────

@Serializable
data class OnionSkinConfig(
    val isEnabled: Boolean = false,
    val mode: OnionSkinMode = OnionSkinMode.CLASSIC,
    val framesBefore: Int = 2,
    val framesAfter: Int = 1,
    val opacityBefore: Float = 0.3f,
    val opacityAfter: Float = 0.2f,
    val tintBefore: SoCreateColor = SoCreateColor(1f, 0f, 0f, 0.3f),    // Red
    val tintAfter: SoCreateColor = SoCreateColor(0f, 1f, 0f, 0.2f),     // Green
    // Enhanced modes
    val blendMode: OnionBlendMode = OnionBlendMode.TINT,
    val showGhostFrames: Boolean = false,
    val ghostOpacity: Float = 0.1f,
    val showMotionTrail: Boolean = false,
    val motionTrailLength: Int = 5,
    val motionTrailColor: SoCreateColor = SoCreateColor(1f, 1f, 0f, 0.15f),
    val fadeWithDistance: Boolean = true,
    val showCurrentOnly: Boolean = false,
    val matchLineWidth: Boolean = false,
    val opacityCurve: OnionOpacityCurve = OnionOpacityCurve.LINEAR,
    // Effect overlays for onion skin
    val effectOverlay: OnionEffectOverlay = OnionEffectOverlay.NONE,
    val effectOverlayOpacity: Float = 0.5f,
    val effectOverlayColor: SoCreateColor = SoCreateColor(0f, 0.5f, 1f, 0.3f)
)

@Serializable
enum class OnionSkinMode(val displayName: String) {
    CLASSIC("Classic"),             // Standard before/after tinting
    MERGE("Merge"),                 // Merge adjacent frames
    DIFFERENCE("Difference"),       // Show only what changed between frames
    OPACITY_BLEND("Opacity Blend"), // Pure opacity stacking
    BLUEPRINT("Blueprint"),         // Blueprint-style outlines only
    SILHOUETTE("Silhouette"),       // Filled silhouettes for motion reference
    XRAY("X-Ray"),                  // Wireframe outlines with fill
    CUSTOM("Custom")                // User-defined tint and blend
}

@Serializable
enum class OnionBlendMode(val displayName: String) {
    TINT("Tint"),                   // Color overlay tinting
    REPLACE("Replace"),             // Replace colors
    MULTIPLY("Multiply"),           // Multiply blend
    SCREEN("Screen"),               // Screen blend
    OVERLAY("Overlay"),             // Overlay blend
    DIFFERENCE("Difference"),       // Difference blend (show changes)
    SUBTRACT("Subtract"),           // Subtract blend
    LIGHTEN("Lighten"),             // Lighten blend
    DARKEN("Darken")                // Darken blend
}

@Serializable
enum class OnionOpacityCurve {
    LINEAR,         // Linear falloff
    EASE_IN,        // Slow start, fast end
    EASE_OUT,       // Fast start, slow end
    STEP            // Binary on/off per frame
}

@Serializable
enum class OnionEffectOverlay(val displayName: String) {
    NONE("None"),
    GLOW("Glow"),                   // Soft glow around onion skin strokes
    CHROMATIC("Chromatic Shift"),    // RGB channel shift effect
    BLUR("Blur"),                    // Motion blur on previous frames
    OUTLINE_ONLY("Outline Only"),    // Only show outlines of onion frames
    NEGATIVE("Negative"),            // Invert colors of onion frames
    POSTERIZE("Posterize"),          // Reduce color count for clarity
    CROSS_HATCH("Cross Hatch")       // Pattern overlay for artistic effect
}

// ─── Layer Outlining (Aseprite-style) ───────────────────────────────────────

@Serializable
data class LayerOutlineConfig(
    val isEnabled: Boolean = false,
    val mode: LayerOutlineMode = LayerOutlineMode.ALL_VISIBLE,
    val color: SoCreateColor = SoCreateColor(0f, 0.8f, 1f, 0.6f),    // Cyan
    val activeLayerColor: SoCreateColor = SoCreateColor(0f, 1f, 0.6f, 0.9f), // Green
    val lockedLayerColor: SoCreateColor = SoCreateColor(1f, 0.3f, 0.3f, 0.4f), // Red
    val outlineWidth: Float = 1.5f,
    val showForActiveOnly: Boolean = false,
    val showForAllLayers: Boolean = true,
    val showForLocked: Boolean = false,
    val showForHidden: Boolean = false,
    val dashPattern: OutlineDashPattern = OutlineDashPattern.SOLID,
    val animateOutline: Boolean = true,         // Marching ants
    val animationSpeed: Float = 1f,
    val outlineOffset: Float = 0f,              // Pixel offset from content edge
    val includeTransparentPixels: Boolean = false,
    val detectionThreshold: Float = 0.5f        // Alpha threshold for edge detection
)

@Serializable
enum class LayerOutlineMode(val displayName: String) {
    ALL_VISIBLE("All Visible Layers"),
    ACTIVE_ONLY("Active Layer Only"),
    SELECTED_LAYERS("Selected Layers"),
    NON_EMPTY("Non-Empty Layers Only"),
    ANIMATION_FRAMES("Animation Frames"),
    CUSTOM("Custom Selection")
}

@Serializable
enum class OutlineDashPattern {
    SOLID,
    DASHED,
    DOTTED,
    DASH_DOT,
    MARCHING_ANTS
}

// ─── Multi-Frame Selection (Aseprite/Resprite) ──────────────────────────────

@Serializable
data class MultiFrameSelection(
    val selectedFrameIds: Set<String> = emptySet(),
    val selectedFrameIndices: Set<Int> = emptySet(),
    val selectionMode: FrameSelectionMode = FrameSelectionMode.RANGE,
    val rangeStart: Int = -1,
    val rangeEnd: Int = -1,
    val isActive: Boolean = false,
    val lastAction: BatchAction? = null
) {
    val count: Int get() = selectedFrameIndices.size
    val isEmpty: Boolean get() = selectedFrameIndices.isEmpty()
    val isRange: Boolean get() = selectionMode == FrameSelectionMode.RANGE && rangeStart >= 0

    fun selectFrame(index: Int): MultiFrameSelection {
        return copy(
            selectedFrameIndices = selectedFrameIndices + index,
            isActive = true
        )
    }

    fun deselectFrame(index: Int): MultiFrameSelection {
        val newSelection = selectedFrameIndices - index
        return copy(
            selectedFrameIndices = newSelection,
            isActive = newSelection.isNotEmpty()
        )
    }

    fun selectRange(start: Int, end: Int): MultiFrameSelection {
        val range = if (start <= end) start..end else end..start
        return copy(
            selectedFrameIndices = range.toSet(),
            rangeStart = start,
            rangeEnd = end,
            selectionMode = FrameSelectionMode.RANGE,
            isActive = true
        )
    }

    fun selectAll(totalFrames: Int): MultiFrameSelection {
        return copy(
            selectedFrameIndices = (0 until totalFrames).toSet(),
            selectionMode = FrameSelectionMode.ALL,
            isActive = true
        )
    }

    fun clear(): MultiFrameSelection {
        return copy(
            selectedFrameIndices = emptySet(),
            selectedFrameIds = emptySet(),
            rangeStart = -1,
            rangeEnd = -1,
            isActive = false
        )
    }

    fun toggleFrame(index: Int): MultiFrameSelection {
        return if (index in selectedFrameIndices) deselectFrame(index)
        else selectFrame(index)
    }
}

@Serializable
enum class FrameSelectionMode {
    SINGLE,         // One frame at a time
    RANGE,          // Shift+click range select
    TOGGLE,         // Ctrl+click toggle individual
    ALL             // Select all frames
}

// ─── Batch Actions for Multi-Frame Selection ────────────────────────────────

@Serializable
sealed class BatchAction {
    @Serializable
    data class Move(val deltaX: Float, val deltaY: Float) : BatchAction()

    @Serializable
    data class Scale(val factor: Float, val pivotX: Float, val pivotY: Float) : BatchAction()

    @Serializable
    data class Rotate(val angle: Float, val pivotX: Float, val pivotY: Float) : BatchAction()

    @Serializable
    data class Flip(val horizontal: Boolean, val vertical: Boolean) : BatchAction()

    @Serializable
    data class SetOpacity(val opacity: Float) : BatchAction()

    @Serializable
    data class SetDuration(val duration: Float) : BatchAction()

    @Serializable
    data class Delete(val frameIndices: Set<Int>) : BatchAction()

    @Serializable
    data class Duplicate(val frameIndices: Set<Int>) : BatchAction()

    @Serializable
    data class Reverse(val frameIndices: List<Int>) : BatchAction()

    @Serializable
    data class ApplyFilter(val filterType: FilterType, val value: Float) : BatchAction()

    @Serializable
    data class ShiftHue(val degrees: Float) : BatchAction()

    @Serializable
    data class ShiftBrightness(val amount: Float) : BatchAction()

    @Serializable
    data class SetBlendMode(val blendMode: BlendMode) : BatchAction()

    @Serializable
    data class OnionSkin(val config: OnionSkinConfig) : BatchAction()

    @Serializable
    data class Label(val label: String) : BatchAction()

    @Serializable
    data class ColorCode(val color: SoCreateColor) : BatchAction()
}

// ─── On-Screen Modifier Keys (Aseprite/Resprite) ────────────────────────────

@Serializable
data class ModifierKeyState(
    val isCtrlPressed: Boolean = false,
    val isShiftPressed: Boolean = false,
    val isAltPressed: Boolean = false,
    val isMetaPressed: Boolean = false,     // Cmd on Mac
    val showOnScreenButtons: Boolean = true,
    val buttonPosition: ModifierButtonPosition = ModifierButtonPosition.BOTTOM_LEFT,
    val buttonSize: Float = 44f,            // dp
    val buttonOpacity: Float = 0.85f,
    val autoHide: Boolean = false,
    val autoHideDelayMs: Long = 3000L,
    val hapticFeedback: Boolean = true
) {
    val isMultiSelect: Boolean get() = isCtrlPressed || isShiftPressed
    val isRangeSelect: Boolean get() = isShiftPressed && !isCtrlPressed
    val isToggleSelect: Boolean get() = isCtrlPressed && !isShiftPressed
    val isAddToSelection: Boolean get() = isShiftPressed
    val isSubtractFromSelection: Boolean get() = isAltPressed && isCtrlPressed
}

@Serializable
enum class ModifierButtonPosition {
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    LEFT_SIDE,
    RIGHT_SIDE,
    CUSTOM
}

// ─── Frame Tags & Color Coding (Aseprite-style) ────────────────────────────

@Serializable
data class FrameTag(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Tag",
    val color: SoCreateColor = SoCreateColor(0.3f, 0.6f, 1f),
    val startFrame: Int = 0,
    val endFrame: Int = 0,
    val repeat: TagRepeat = TagRepeat.INFINITE,
    val repeatCount: Int = 0,
    val easing: EasingType = EasingType.LINEAR
)

@Serializable
enum class TagRepeat {
    INFINITE,
    ONCE,
    CUSTOM
}

// ─── Frame Color Coding ─────────────────────────────────────────────────────

@Serializable
enum class FrameColor(val displayName: String, val color: SoCreateColor) {
    NONE("None", SoCreateColor.TRANSPARENT),
    RED("Red", SoCreateColor(1f, 0.2f, 0.2f)),
    ORANGE("Orange", SoCreateColor(1f, 0.6f, 0.2f)),
    YELLOW("Yellow", SoCreateColor(1f, 0.9f, 0.2f)),
    GREEN("Green", SoCreateColor(0.2f, 0.9f, 0.3f)),
    CYAN("Cyan", SoCreateColor(0.2f, 0.8f, 1f)),
    BLUE("Blue", SoCreateColor(0.2f, 0.4f, 1f)),
    PURPLE("Purple", SoCreateColor(0.6f, 0.2f, 1f)),
    PINK("Pink", SoCreateColor(1f, 0.3f, 0.6f)),
    WHITE("White", SoCreateColor.WHITE)
}

// ─── Cel Linking (Aseprite-style linked frames) ────────────────────────────

@Serializable
data class CelLink(
    val sourceFrameIndex: Int,
    val sourceLayerId: String,
    val targetFrameIndex: Int,
    val targetLayerId: String
) {
    /** Linked cels share the same pixel data — editing one updates all */
    val isLinked: Boolean get() = true
}
