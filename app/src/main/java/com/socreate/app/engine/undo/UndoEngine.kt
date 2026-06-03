package com.socreate.app.engine.undo

import com.socreate.app.core.model.*
import java.util.Stack

/**
 * Non-destructive undo/redo engine.
 *
 * Stores state snapshots or deltas to allow unlimited undo/redo.
 * Uses a command pattern where each action is recorded as a reversible entry.
 *
 * Memory-efficient: stores deltas rather than full copies where possible.
 */
class UndoEngine(
    private val maxHistorySize: Int = 100
) {
    private val undoStack: Stack<UndoEntry> = Stack()
    private val redoStack: Stack<UndoEntry> = Stack()

    val canUndo: Boolean get() = undoStack.isNotEmpty()
    val canRedo: Boolean get() = redoStack.isNotEmpty()
    val undoCount: Int get() = undoStack.size
    val redoCount: Int get() = redoStack.size

    /**
     * Record an action for potential undo.
     */
    fun record(entry: UndoEntry) {
        undoStack.push(entry)
        redoStack.clear() // New action invalidates redo history

        // Trim history if it exceeds the maximum
        while (undoStack.size > maxHistorySize) {
            undoStack.removeAt(0)
        }
    }

    /**
     * Record a layer stack change.
     */
    fun recordLayerChange(
        description: String,
        previousState: LayerStack,
        newState: LayerStack
    ) {
        record(
            UndoEntry.LayerStackChange(
                description = description,
                previousState = previousState,
                newState = newState
            )
        )
    }

    /**
     * Record a completed stroke.
     */
    fun recordStroke(
        layerId: String,
        stroke: Stroke
    ) {
        record(
            UndoEntry.StrokeAdded(
                layerId = layerId,
                stroke = stroke
            )
        )
    }

    /**
     * Record a deleted stroke.
     */
    fun recordStrokeRemoved(
        layerId: String,
        stroke: Stroke,
        index: Int
    ) {
        record(
            UndoEntry.StrokeRemoved(
                layerId = layerId,
                stroke = stroke,
                index = index
            )
        )
    }

    /**
     * Undo the last action and return the entry describing what to reverse.
     */
    fun undo(): UndoEntry? {
        if (undoStack.isEmpty()) return null
        val entry = undoStack.pop()
        redoStack.push(entry)
        return entry
    }

    /**
     * Redo the last undone action.
     */
    fun redo(): UndoEntry? {
        if (redoStack.isEmpty()) return null
        val entry = redoStack.pop()
        undoStack.push(entry)
        return entry
    }

    /**
     * Clear all undo/redo history.
     */
    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}

/**
 * Represents a single undoable/redoable action.
 */
sealed class UndoEntry(
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * A complete layer stack change (layer added, removed, reordered, etc.)
     */
    data class LayerStackChange(
        override val description: String,
        val previousState: LayerStack,
        val newState: LayerStack
    ) : UndoEntry(description)

    /**
     * A stroke was added to a layer.
     */
    data class StrokeAdded(
        val layerId: String,
        val stroke: Stroke
    ) : UndoEntry("Stroke on $layerId")

    /**
     * A stroke was removed from a layer.
     */
    data class StrokeRemoved(
        val layerId: String,
        val stroke: Stroke,
        val index: Int
    ) : UndoEntry("Stroke removed")

    /**
     * A transform was applied.
     */
    data class TransformApplied(
        val layerId: String,
        val previousTransform: LayerTransform,
        val newTransform: LayerTransform
    ) : UndoEntry("Transform")

    /**
     * Layer effects were changed.
     */
    data class EffectsChanged(
        val layerId: String,
        val previousEffects: LayerEffects,
        val newEffects: LayerEffects
    ) : UndoEntry("Effects changed")

    /**
     * Canvas was cleared.
     */
    data class CanvasCleared(
        val previousStrokes: Map<String, StrokeCollection>
    ) : UndoEntry("Canvas cleared")

    /**
     * A selection was filled.
     */
    data class SelectionFilled(
        val layerId: String,
        val color: SoCreateColor,
        val previousPixels: ByteArray? = null
    ) : UndoEntry("Fill") {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SelectionFilled) return false
            return layerId == other.layerId && color == other.color
        }
        override fun hashCode(): Int = 31 * layerId.hashCode() + color.hashCode()
    }
}
