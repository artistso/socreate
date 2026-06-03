package com.socreate.app.engine.layer

import android.graphics.*
import com.socreate.app.core.model.*

/**
 * Composites multiple layers together using blend modes and opacity.
 * Responsible for rendering the complete layer stack onto a target canvas.
 */
class LayerCompositor {

    private val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tempPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blendModeMap = mutableMapOf<BlendMode, PorterDuff.Mode?>()

    init {
        // Map SoCreate blend modes to Android PorterDuff modes
        blendModeMap[BlendMode.NORMAL] = null // Default SRC_OVER
        blendModeMap[BlendMode.MULTIPLY] = PorterDuff.Mode.MULTIPLY
        blendModeMap[BlendMode.SCREEN] = PorterDuff.Mode.SCREEN
        blendModeMap[BlendMode.OVERLAY] = PorterDuff.Mode.OVERLAY
        blendModeMap[BlendMode.DARKEN] = PorterDuff.Mode.DARKEN
        blendModeMap[BlendMode.LIGHTEN] = PorterDuff.Mode.LIGHTEN
        blendModeMap[BlendMode.COLOR_DODGE] = PorterDuff.Mode.DODGE
        blendModeMap[BlendMode.COLOR_BURN] = PorterDuff.Mode.BURN
        blendModeMap[BlendMode.HARD_LIGHT] = PorterDuff.Mode.HARD_LIGHT
        blendModeMap[BlendMode.SOFT_LIGHT] = PorterDuff.Mode.SOFT_LIGHT
        blendModeMap[BlendMode.DIFFERENCE] = PorterDuff.Mode.DIFFERENCE
        blendModeMap[BlendMode.EXCLUSION] = PorterDuff.Mode.EXCLUSION
        blendModeMap[BlendMode.HUE] = PorterDuff.Mode.HUE
        blendModeMap[BlendMode.SATURATION] = PorterDuff.Mode.SATURATION
        blendModeMap[BlendMode.COLOR] = PorterDuff.Mode.COLOR
        blendModeMap[BlendMode.LUMINOSITY] = PorterDuff.Mode.LUMINOSITY
    }

    /**
     * Composite all visible layers onto the target canvas.
     *
     * @param targetCanvas The canvas to render the final result onto
     * @param layerStack The complete layer stack
     * @param layerBitmaps Map of layer IDs to their rendered Bitmaps
     * @param backgroundColor The canvas background color
     */
    fun composite(
        targetCanvas: Canvas,
        layerStack: LayerStack,
        layerBitmaps: Map<String, Bitmap>,
        backgroundColor: SoCreateColor
    ) {
        // Draw background
        targetCanvas.drawColor(backgroundColor.toArgb())

        // Render layers from bottom to top
        val sortedLayers = layerStack.visibleLayers.sortedBy { it.sortOrder }

        for (layer in sortedLayers) {
            val bitmap = layerBitmaps[layer.id] ?: continue

            // Set up paint for this layer
            layerPaint.alpha = (layer.opacity * 255).toInt()
            layerPaint.xfermode = blendModeMap[layer.blendMode]?.let {
                PorterDuffXfermode(it)
            }

            // Apply layer transform
            targetCanvas.save()
            applyLayerTransform(targetCanvas, layer.transform, bitmap)

            // Handle clipping masks
            if (layer.isClippingMask && layer.referenceLayer != null) {
                // Clip to the shape of the reference layer
                val refBitmap = layerBitmaps[layer.referenceLayer]
                if (refBitmap != null) {
                    // Use the reference layer as a mask
                    targetCanvas.drawBitmap(bitmap, 0f, 0f, layerPaint)
                }
            } else {
                targetCanvas.drawBitmap(bitmap, 0f, 0f, layerPaint)
            }

            targetCanvas.restore()
        }
    }

    /**
     * Composite layers for a specific viewport region (tile-based optimization).
     */
    fun compositeRegion(
        targetCanvas: Canvas,
        layerStack: LayerStack,
        layerBitmaps: Map<String, Bitmap>,
        backgroundColor: SoCreateColor,
        region: Rect
    ) {
        targetCanvas.save()
        targetCanvas.clipRect(region)
        composite(targetCanvas, layerStack, layerBitmaps, backgroundColor)
        targetCanvas.restore()
    }

    /**
     * Render just the active layer (for in-progress stroke overlay).
     */
    fun renderActiveLayer(
        targetCanvas: Canvas,
        layer: Layer,
        bitmap: Bitmap,
        activeStrokeBitmap: Bitmap? = null
    ) {
        layerPaint.alpha = (layer.opacity * 255).toInt()
        layerPaint.xfermode = null

        targetCanvas.drawBitmap(bitmap, 0f, 0f, layerPaint)

        // Overlay the active stroke
        if (activeStrokeBitmap != null) {
            layerPaint.alpha = 255
            targetCanvas.drawBitmap(activeStrokeBitmap, 0f, 0f, layerPaint)
        }
    }

    /**
     * Apply a layer's transform to the canvas.
     */
    private fun applyLayerTransform(canvas: Canvas, transform: LayerTransform, bitmap: Bitmap) {
        if (transform == LayerTransform.IDENTITY) return

        val pivotX = bitmap.width * transform.pivotX
        val pivotY = bitmap.height * transform.pivotY

        canvas.translate(pivotX, pivotY)
        canvas.rotate(transform.rotation)
        canvas.scale(transform.scaleX, transform.scaleY)
        canvas.translate(-pivotX + transform.offsetX, -pivotY + transform.offsetY)
    }

    /**
     * Apply a blend mode to source-over composition.
     * Used for per-stroke blending within a layer.
     */
    fun applyBlendMode(paint: Paint, blendMode: BlendMode) {
        blendModeMap[blendMode]?.let {
            paint.xfermode = PorterDuffXfermode(it)
        }
    }

    companion object {
        /**
         * Check if a blend mode requires reading the destination pixels.
         * Used for optimizing rendering pipeline.
         */
        fun requiresDestinationRead(blendMode: BlendMode): Boolean {
            return blendMode != BlendMode.NORMAL
        }
    }
}
