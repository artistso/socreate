package com.socreate.app.engine.renderer

import com.socreate.app.core.model.*

/**
 * Enhanced Onion Skin Renderer with multiple modes and effect overlays.
 *
 * Renders previous and next frames with configurable blend modes,
 * tint colors, opacity curves, and special effect overlays.
 *
 * Modes:
 * - Classic: Standard red/green tinting (default)
 * - Merge: Merge adjacent frame content
 * - Difference: Show only what changed between frames
 * - Blueprint: Technical drawing style with outlines only
 * - Silhouette: Filled shapes for motion reference
 * - X-Ray: Wireframe outlines with transparent fill
 *
 * Effect Overlays:
 * - Glow: Soft glow around onion skin strokes
 * - Chromatic Shift: RGB channel offset
 * - Blur: Motion blur on past frames
 * - Outline Only: Only edges of onion skin content
 * - Negative: Inverted colors
 * - Posterize: Reduced color count
 * - Cross Hatch: Pattern overlay
 *
 * Inspired by Aseprite, Resprite, and Clip Studio Paint onion skin features.
 */
class OnionSkinRenderer {

    /**
     * Calculate the opacity for a given frame offset based on the config.
     */
    fun calculateFrameOpacity(
        frameOffset: Int,  // Negative = before, Positive = after, 0 = current
        config: OnionSkinConfig
    ): Float {
        if (!config.isEnabled || frameOffset == 0) return 0f

        val isBefore = frameOffset < 0
        val absOffset = kotlin.math.abs(frameOffset)

        // Check if within range
        val maxFrames = if (isBefore) config.framesBefore else config.framesAfter
        if (absOffset > maxFrames) return 0f

        val baseOpacity = if (isBefore) config.opacityBefore else config.opacityAfter

        return if (config.fadeWithDistance && maxFrames > 0) {
            val factor = when (config.opacityCurve) {
                OnionOpacityCurve.LINEAR -> 1f - (absOffset - 1).toFloat() / maxFrames
                OnionOpacityCurve.EASE_IN -> {
                    val t = (absOffset - 1).toFloat() / maxFrames
                    t * t  // Quadratic ease in
                }
                OnionOpacityCurve.EASE_OUT -> {
                    val t = (absOffset - 1).toFloat() / maxFrames
                    1f - (1f - t) * (1f - t)  // Quadratic ease out
                }
                OnionOpacityCurve.STEP -> if (absOffset == 1) 1f else 0f
            }
            (baseOpacity * factor).coerceIn(0f, 1f)
        } else {
            baseOpacity
        }
    }

    /**
     * Get the tint color for a given frame offset.
     */
    fun getTintColor(frameOffset: Int, config: OnionSkinConfig): SoCreateColor {
        val isBefore = frameOffset < 0
        val baseTint = if (isBefore) config.tintBefore else config.tintAfter
        val opacity = calculateFrameOpacity(frameOffset, config)
        return baseTint.withAlpha(opacity)
    }

    /**
     * Get the tint color for a specific onion skin mode.
     */
    fun getModeTintColor(frameOffset: Int, config: OnionSkinConfig): SoCreateColor {
        return when (config.mode) {
            OnionSkinMode.CLASSIC -> getTintColor(frameOffset, config)
            OnionSkinMode.MERGE -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                SoCreateColor.WHITE.withAlpha(opacity * 0.8f)
            }
            OnionSkinMode.DIFFERENCE -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                SoCreateColor(1f, 1f, 0f, opacity)  // Yellow for differences
            }
            OnionSkinMode.BLUEPRINT -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                SoCreateColor(0.2f, 0.5f, 1f, opacity)  // Blueprint blue
            }
            OnionSkinMode.SILHOUETTE -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                if (frameOffset < 0) SoCreateColor(1f, 0f, 0f, opacity)
                else SoCreateColor(0f, 0.8f, 0f, opacity)
            }
            OnionSkinMode.XRAY -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                SoCreateColor(0f, 0.8f, 1f, opacity)  // Cyan wireframe
            }
            OnionSkinMode.CUSTOM -> getTintColor(frameOffset, config)
            OnionSkinMode.OPACITY_BLEND -> {
                val opacity = calculateFrameOpacity(frameOffset, config)
                SoCreateColor.WHITE.withAlpha(opacity)
            }
        }
    }

    /**
     * Determine which frame indices should be rendered as onion skin.
     */
    fun getVisibleOnionFrames(
        currentFrame: Int,
        totalFrames: Int,
        config: OnionSkinConfig
    ): List<OnionFrameInfo> {
        if (!config.isEnabled) return emptyList()

        val frames = mutableListOf<OnionFrameInfo>()

        // Previous frames
        for (i in 1..config.framesBefore) {
            val frameIndex = currentFrame - i
            if (frameIndex >= 0) {
                frames.add(OnionFrameInfo(
                    frameIndex = frameIndex,
                    offset = -i,
                    opacity = calculateFrameOpacity(-i, config),
                    tintColor = getModeTintColor(-i, config),
                    isGhost = config.showGhostFrames && i > config.framesBefore
                ))
            }
        }

        // Next frames
        for (i in 1..config.framesAfter) {
            val frameIndex = currentFrame + i
            if (frameIndex < totalFrames) {
                frames.add(OnionFrameInfo(
                    frameIndex = frameIndex,
                    offset = i,
                    opacity = calculateFrameOpacity(i, config),
                    tintColor = getModeTintColor(i, config),
                    isGhost = config.showGhostFrames && i > config.framesAfter
                ))
            }
        }

        // Ghost frames (additional very faint frames)
        if (config.showGhostFrames) {
            for (i in (config.framesBefore + 1)..(config.framesBefore + 3)) {
                val frameIndex = currentFrame - i
                if (frameIndex >= 0) {
                    frames.add(OnionFrameInfo(
                        frameIndex = frameIndex,
                        offset = -i,
                        opacity = config.ghostOpacity * (1f - (i - config.framesBefore) / 4f),
                        tintColor = getModeTintColor(-i, config).withAlpha(config.ghostOpacity),
                        isGhost = true
                    ))
                }
            }
        }

        // Motion trail frames
        if (config.showMotionTrail) {
            for (i in 1..config.motionTrailLength) {
                val frameIndex = currentFrame - i
                if (frameIndex >= 0) {
                    val existing = frames.find { it.frameIndex == frameIndex }
                    if (existing == null) {
                        val trailOpacity = config.motionTrailColor.alpha * (1f - i.toFloat() / config.motionTrailLength)
                        frames.add(OnionFrameInfo(
                            frameIndex = frameIndex,
                            offset = -i,
                            opacity = trailOpacity,
                            tintColor = config.motionTrailColor.withAlpha(trailOpacity),
                            isMotionTrail = true
                        ))
                    }
                }
            }
        }

        return frames.sortedBy { it.offset }
    }

    /**
     * Get the effect overlay parameters for rendering.
     */
    fun getEffectOverlayConfig(config: OnionSkinConfig): EffectOverlayParams {
        return when (config.effectOverlay) {
            OnionEffectOverlay.NONE -> EffectOverlayParams.None
            OnionEffectOverlay.GLOW -> EffectOverlayParams.Glow(
                radius = 8f,
                color = config.effectOverlayColor,
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.CHROMATIC -> EffectOverlayParams.ChromaticShift(
                offsetX = 2f,
                offsetY = 1f,
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.BLUR -> EffectOverlayParams.Blur(
                radius = 3f,
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.OUTLINE_ONLY -> EffectOverlayParams.OutlineOnly(
                width = 2f,
                color = config.effectOverlayColor,
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.NEGATIVE -> EffectOverlayParams.Negative(
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.POSTERIZE -> EffectOverlayParams.Posterize(
                levels = 4,
                opacity = config.effectOverlayOpacity
            )
            OnionEffectOverlay.CROSS_HATCH -> EffectOverlayParams.CrossHatch(
                spacing = 6f,
                angle = 45f,
                opacity = config.effectOverlayOpacity
            )
        }
    }
}

data class OnionFrameInfo(
    val frameIndex: Int,
    val offset: Int,
    val opacity: Float,
    val tintColor: SoCreateColor,
    val isGhost: Boolean = false,
    val isMotionTrail: Boolean = false
)

sealed class EffectOverlayParams {
    object None : EffectOverlayParams()
    data class Glow(val radius: Float, val color: SoCreateColor, val opacity: Float) : EffectOverlayParams()
    data class ChromaticShift(val offsetX: Float, val offsetY: Float, val opacity: Float) : EffectOverlayParams()
    data class Blur(val radius: Float, val opacity: Float) : EffectOverlayParams()
    data class OutlineOnly(val width: Float, val color: SoCreateColor, val opacity: Float) : EffectOverlayParams()
    data class Negative(val opacity: Float) : EffectOverlayParams()
    data class Posterize(val levels: Int, val opacity: Float) : EffectOverlayParams()
    data class CrossHatch(val spacing: Float, val angle: Float, val opacity: Float) : EffectOverlayParams()
}
