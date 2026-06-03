package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents the complete canvas configuration.
 * Defaults are optimized for Samsung Galaxy Tab S10+ (2800×1752).
 */
@Serializable
data class Canvas(
    val width: Int = 2800,
    val height: Int = 1752,
    val dpi: Int = 266,
    val backgroundColor: SoCreateColor = SoCreateColor.WHITE,
    val colorProfile: ColorProfile = ColorProfile.DISPLAY_P3,
    val orientation: CanvasOrientation = CanvasOrientation.LANDSCAPE
) {
    val aspectRatio: Float get() = width.toFloat() / height.toFloat()

    companion object {
        // Use Tab S10+ optimized presets from TabS10Plus object
        val DEFAULT = TabS10Plus.NATIVE
        val NATIVE = TabS10Plus.NATIVE
        val ART_4K = TabS10Plus.ART_4K
        val YOUTUBE_4K = TabS10Plus.YOUTUBE_4K
        val INSTAGRAM_SQUARE = TabS10Plus.INSTAGRAM_SQUARE
        val VERTICAL_VIDEO = TabS10Plus.VERTICAL_VIDEO
        val A4_PRINT = TabS10Plus.A4_PRINT
        val NATIVE_PORTRAIT = TabS10Plus.NATIVE_PORTRAIT

        val PRESETS = TabS10Plus.PRESETS
    }
}

@Serializable
enum class ColorProfile {
    SRGB, DISPLAY_P3, ADOBE_RGB, LINEAR_SRGB
}

@Serializable
enum class CanvasOrientation {
    LANDSCAPE, PORTRAIT, SQUARE
}

/**
 * Represents a complete SoCreate project/document.
 */
@Serializable
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Untitled Artwork",
    val canvas: Canvas = Canvas.DEFAULT,
    val layerStack: LayerStack = LayerStack.createDefault(),
    val animationTimeline: AnimationTimeline? = null,
    val thumbnailPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val version: Int = 1,
    val metadata: ProjectMetadata = ProjectMetadata()
) {
    fun updateModified() = copy(modifiedAt = System.currentTimeMillis())
    fun rename(newName: String) = copy(name = newName, modifiedAt = System.currentTimeMillis())
}

@Serializable
data class ProjectMetadata(
    val author: String = "",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val totalDuration: Long = 0,
    val frameCount: Int = 0,
    val appVersion: String = "1.0.0",
    /** Target device the project was created on. */
    val targetDevice: String = "Samsung Galaxy Tab S10+"
)

// ─── Animation Models (Foundation for future phases) ────────────────────────

@Serializable
data class AnimationTimeline(
    val tracks: List<AnimationTrack> = emptyList(),
    val frameRate: Int = 24,
    val totalFrames: Int = 24,
    val currentTime: Int = 0
) {
    val durationMs: Long get() = (totalFrames * 1000L) / frameRate
    val currentProgress: Float get() = if (totalFrames > 0) currentTime.toFloat() / totalFrames else 0f
    fun atFrame(frame: Int): AnimationTimeline = copy(currentTime = frame.coerceIn(0, totalFrames - 1))
}

@Serializable
data class AnimationTrack(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Track",
    val type: TrackType = TrackType.FLIPBOOK,
    val layerId: String = "",
    val keyframes: List<Keyframe> = emptyList(),
    val isEnabled: Boolean = true,
    val isLocked: Boolean = false,
    val isMuted: Boolean = false,
    val onionSkinning: OnionSkinConfig = OnionSkinConfig()
)

@Serializable
enum class TrackType {
    FLIPBOOK, KEYFRAME, PERFORMING, AUDIO, VIDEO
}

@Serializable
data class Keyframe(
    val id: String = UUID.randomUUID().toString(),
    val frameIndex: Int = 0,
    val data: KeyframeData = KeyframeData.Transform(),
    val easing: EasingType = EasingType.LINEAR,
    val isHold: Boolean = false
)

@Serializable
sealed class KeyframeData {
    @Serializable
    data class Transform(
        val offsetX: Float = 0f,
        val offsetY: Float = 0f,
        val scaleX: Float = 1f,
        val scaleY: Float = 1f,
        val rotation: Float = 0f,
        val opacity: Float = 1f
    ) : KeyframeData()

    @Serializable
    data class Warp(
        val controlPoints: List<ControlPoint> = emptyList()
    ) : KeyframeData()

    @Serializable
    data class Filter(
        val filters: List<FilterParameter> = emptyList()
    ) : KeyframeData()
}

@Serializable
data class FilterParameter(
    val type: FilterType,
    val value: Float = 0f
)

@Serializable
enum class FilterType {
    BLUR, SHARPEN, BRIGHTNESS, CONTRAST, SATURATION,
    HUE_SHIFT, NOISE, PIXELATE, COLOR_BALANCE
}

@Serializable
enum class EasingType {
    LINEAR, EASE_IN, EASE_OUT, EASE_IN_OUT,
    SPRING, BOUNCE, ELASTIC, CUSTOM
}

// OnionSkinConfig has been moved to AnimationAdvanced.kt with enhanced features


