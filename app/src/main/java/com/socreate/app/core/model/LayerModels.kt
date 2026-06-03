package com.socreate.app.core.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Represents a single layer in the drawing canvas.
 * Supports blend modes, opacity, visibility, locking, and masking.
 */
@Serializable
data class Layer(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Layer",
    val type: LayerType = LayerType.RASTER,
    val opacity: Float = 1f,               // 0..1
    val blendMode: BlendMode = BlendMode.NORMAL,
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val isClippingMask: Boolean = false,
    val referenceLayer: String? = null,    // ID of reference layer for clipping
    val mask: LayerMask? = null,
    val transform: LayerTransform = LayerTransform(),
    val effects: LayerEffects = LayerEffects(),
    val sortOrder: Int = 0                 // Z-order (0 = bottom)
) {
    fun withOpacity(opacity: Float) = copy(opacity = opacity.coerceIn(0f, 1f))
    fun withVisible(visible: Boolean) = copy(isVisible = visible)
    fun withLocked(locked: Boolean) = copy(isLocked = locked)
    fun withBlendMode(mode: BlendMode) = copy(blendMode = mode)
    fun withName(name: String) = copy(name = name)
    fun withSortOrder(order: Int) = copy(sortOrder = order)
}

@Serializable
enum class LayerType {
    RASTER,             // Standard pixel layer
    VECTOR,             // Vector path layer (for line work)
    GROUP,              // Group of child layers
    ADJUSTMENT,         // Adjustment layer (filters)
    TEXT,               // Text layer
    REFERENCE,          // Reference image layer (non-rendering)
    ANIMATION_FLIPBOOK, // Flipbook animation layer with frames
    ANIMATION_KEYFRAME  // Keyframe animation layer
}

@Serializable
data class LayerMask(
    val id: String = UUID.randomUUID().toString(),
    val isEnabled: Boolean = true,
    val isLinked: Boolean = true           // Linked to layer transform
)

@Serializable
data class LayerTransform(
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotation: Float = 0f,             // degrees
    val pivotX: Float = 0.5f,             // 0..1 normalized
    val pivotY: Float = 0.5f
) {
    companion object {
        val IDENTITY = LayerTransform()
    }
}

@Serializable
data class LayerEffects(
    val dropShadow: DropShadow? = null,
    val innerShadow: InnerShadow? = null,
    val outerGlow: OuterGlow? = null,
    val innerGlow: InnerGlow? = null,
    val stroke: StrokeEffect? = null
)

@Serializable
data class DropShadow(
    val color: SoCreateColor = SoCreateColor.BLACK.withAlpha(0.5f),
    val offsetX: Float = 4f,
    val offsetY: Float = 4f,
    val blur: Float = 10f,
    val spread: Float = 0f
)

@Serializable
data class InnerShadow(
    val color: SoCreateColor = SoCreateColor.BLACK.withAlpha(0.5f),
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val blur: Float = 5f,
    val spread: Float = 0f
)

@Serializable
data class OuterGlow(
    val color: SoCreateColor = SoCreateColor.WHITE.withAlpha(0.5f),
    val blur: Float = 10f,
    val spread: Float = 0f
)

@Serializable
data class InnerGlow(
    val color: SoCreateColor = SoCreateColor.WHITE.withAlpha(0.5f),
    val blur: Float = 5f,
    val spread: Float = 0f
)

@Serializable
data class StrokeEffect(
    val color: SoCreateColor = SoCreateColor.BLACK,
    val width: Float = 2f,
    val position: StrokePosition = StrokePosition.OUTSIDE
)

@Serializable
enum class StrokePosition {
    INSIDE, CENTER, OUTSIDE
}

/**
 * Represents the complete layer stack for a canvas.
 */
@Serializable
data class LayerStack(
    val layers: List<Layer> = emptyList(),
    val activeLayerId: String? = null
) {
    val activeLayer: Layer?
        get() = layers.find { it.id == activeLayerId }

    val visibleLayers: List<Layer>
        get() = layers.filter { it.isVisible }.sortedBy { it.sortOrder }

    fun addLayer(layer: Layer, index: Int = layers.size): LayerStack {
        val updatedLayers = layers.toMutableList().apply {
            add(index.coerceIn(0, size), layer)
        }
        return copy(layers = updatedLayers, activeLayerId = layer.id)
    }

    fun removeLayer(layerId: String): LayerStack {
        val updatedLayers = layers.filter { it.id != layerId }
        val newActiveId = if (activeLayerId == layerId) {
            updatedLayers.lastOrNull()?.id
        } else activeLayerId
        return copy(layers = updatedLayers, activeLayerId = newActiveId)
    }

    fun updateLayer(layerId: String, transform: (Layer) -> Layer): LayerStack {
        val updatedLayers = layers.map { layer ->
            if (layer.id == layerId) transform(layer) else layer
        }
        return copy(layers = updatedLayers)
    }

    fun moveLayer(fromIndex: Int, toIndex: Int): LayerStack {
        val mutableLayers = layers.toMutableList()
        val layer = mutableLayers.removeAt(fromIndex)
        mutableLayers.add(toIndex.coerceIn(0, mutableLayers.size), layer)
        val reindexed = mutableLayers.mapIndexed { index, l -> l.withSortOrder(index) }
        return copy(layers = reindexed)
    }

    fun setActiveLayer(layerId: String): LayerStack {
        return copy(activeLayerId = layerId)
    }

    fun mergeDown(layerId: String): LayerStack {
        val index = layers.indexOfFirst { it.id == layerId }
        if (index <= 0) return this

        val upper = layers[index]
        val lower = layers[index - 1]
        val merged = lower.copy(
            id = lower.id,
            name = "${lower.name} + ${upper.name}"
        )

        val updatedLayers = layers.toMutableList()
        updatedLayers.removeAt(index)     // Remove upper
        updatedLayers[index - 1] = merged // Replace lower with merged

        return copy(layers = updatedLayers, activeLayerId = merged.id)
    }

    companion object {
        /** Create a default layer stack with one empty layer */
        fun createDefault(): LayerStack {
            val firstLayer = Layer(
                name = "Layer 1",
                type = LayerType.RASTER,
                sortOrder = 0
            )
            return LayerStack(
                layers = listOf(firstLayer),
                activeLayerId = firstLayer.id
            )
        }
    }
}
