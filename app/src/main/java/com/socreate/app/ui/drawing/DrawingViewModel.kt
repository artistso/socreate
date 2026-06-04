package com.socreate.app.ui.drawing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socreate.app.core.model.*
import com.socreate.app.core.mvi.*
import com.socreate.app.data.repository.CanvasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Main ViewModel for drawing canvas
 * Manages all drawing state, layers, timeline, and UI
 */
@HiltViewModel
class DrawingViewModel @Inject constructor(
    application: Application,
    private val repository: CanvasRepository
) : AndroidViewModel(application) {
    
    private val _state = MutableStateFlow(DrawingState.initial())
    val state: StateFlow<DrawingState> = _state.asStateFlow()
    
    // Undo/Redo
    private var undoStack = mutableListOf<DrawingState>()
    private var redoStack = mutableListOf<DrawingState>()
    
    init {
        loadSavedState()
    }
    
    // ===== Brush Operations =====
    fun setBrushType(type: BrushType) {
        saveUndoState()
        _state.update { it.copy(brush = it.brush.copy(type = type)) }
    }
    
    fun setBrushSize(size: Float) {
        _state.update { it.copy(brush = it.brush.copy(size = size.coerceIn(1f, 100f))) }
    }
    
    fun setBrushOpacity(opacity: Float) {
        _state.update { it.copy(brush = it.brush.copy(opacity = opacity.coerceIn(0f, 100f))) }
    }
    
    fun setBrushHardness(hardness: Float) {
        _state.update { it.copy(brush = it.brush.copy(hardness = hardness.coerceIn(0f, 100f))) }
    }
    
    fun setBrushColor(color: androidx.compose.ui.graphics.Color) {
        val recent = mutableListOf<Color>().apply {
            add(color)
            addAll(state.value.brush.recentColors.filter { it != color })
        }.take(12)
        _state.update { it.copy(brush = it.brush.copy(color = color, recentColors = recent)) }
    }
    
    // ===== Symmetry =====
    fun setSymmetry(mode: SymmetryMode) {
        _state.update { it.copy(symmetry = mode) }
    }
    
    // ===== Shape Detection =====
    fun setShapeDetection(enabled: Boolean) {
        _state.update { it.copy(shapeDetection = enabled) }
    }
    
    // ===== Layer Operations =====
    fun addLayer(name: String? = null) {
        saveUndoState()
        val layer = LayerState(
            id = UUID.randomUUID().toString(),
            name = name ?: "Layer ${state.value.layers.size + 1}",
            color = generateLayerColor()
        )
        _state.update { it.copy(
            layers = it.layers + layer,
            activeLayerId = layer.id
        )}
    }
    
    fun selectLayer(id: String) {
        _state.update { it.copy(activeLayerId = id) }
    }
    
    fun deleteLayer(id: String) {
        if (state.value.layers.size <= 1) return
        saveUndoState()
        val newLayers = state.value.layers.filter { it.id != id }
        val newActive = if (state.value.activeLayerId == id) {
            newLayers.lastOrNull()?.id
        } else state.value.activeLayerId
        
        _state.update { it.copy(
            layers = newLayers,
            activeLayerId = newActive
        )}
    }
    
    fun toggleLayerVisibility(id: String) {
        _state.update { state ->
            state.copy(
                layers = state.layers.map { l ->
                    if (l.id == id) l.copy(isVisible = !l.isVisible) else l
                }
            )
        }
    }
    
    fun setLayerOpacity(id: String, opacity: Float) {
        _state.update { state ->
            state.copy(
                layers = state.layers.map { l ->
                    if (l.id == id) l.copy(opacity = opacity) else l
                }
            )
        }
    }
    
    fun reorderLayers(fromIndex: Int, toIndex: Int) {
        saveUndoState()
        val layers = state.value.layers.toMutableList()
        if (fromIndex in layers.indices && toIndex in layers.indices) {
            val item = layers.removeAt(fromIndex)
            layers.add(toIndex, item)
            _state.update { it.copy(layers = layers) }
        }
    }
    
    // ===== Stroke Operations =====
    fun startStroke(point: androidx.compose.ui.geometry.Offset) {
        _state.update { it.copy(
            isDrawing = true,
            currentStroke = StrokeData(
                id = UUID.randomUUID().toString(),
                points = listOf(point),
                brushType = it.brush.type,
                color = it.brush.color,
                size = it.brush.size,
                opacity = it.brush.opacity,
                pressurePoints = listOf(1f)
            )
        )}
    }
    
    fun moveStroke(point: androidx.compose.ui.geometry.Offset, pressure: Float) {
        val currentStroke = state.value.currentStroke ?: return
        _state.update { it.copy(
            currentStroke = currentStroke.copy(
                points = currentStroke.points + point,
                pressurePoints = currentStroke.pressurePoints + pressure
            )
        )}
    }
    
    fun endStroke() {
        val currentStroke = state.value.currentStroke ?: return
        saveUndoState()
        
        _state.update { it.copy(
            isDrawing = false,
            currentStroke = null,
            undoStack = it.undoStack + currentStroke,
            redoStack = emptyList()
        )}
        redoStack.clear()
        
        // Save to current frame
        saveFrameState()
    }
    
    // ===== Undo/Redo =====
    fun undo() {
        val stack = state.value.undoStack
        if (stack.isEmpty()) return
        
        saveUndoState()
        val lastStroke = stack.last()
        
        _state.update { it.copy(
            undoStack = it.undoStack.dropLast(1),
            redoStack = it.redoStack + lastStroke
        )}
        redoStack.add(lastStroke)
    }
    
    fun redo() {
        val stack = state.value.redoStack
        if (stack.isEmpty()) return
        
        saveUndoState()
        val lastStroke = stack.last()
        
        _state.update { it.copy(
            undoStack = it.undoStack + lastStroke,
            redoStack = it.redoStack.dropLast(1)
        )}
    }
    
    private fun saveUndoState() {
        // Deep copy current state for undo
        undoStack.add(state.value.copy(
            undoStack = emptyList(),
            redoStack = emptyList(),
            currentStroke = null
        ))
        if (undoStack.size > 50) undoStack.removeAt(0)
    }
    
    // ===== Timeline Operations =====
    fun addFrame() {
        saveUndoState()
        val frameCount = state.value.timeline.frames.size
        val frame = FrameData(
            id = UUID.randomUUID().toString(),
            frameNumber = frameCount,
            isKeyframe = false
        )
        
        _state.update { it.copy(
            timeline = it.timeline.copy(
                frames = it.timeline.frames + frame,
                currentFrameIndex = frameCount
            )
        )}
        saveFrameState()
    }
    
    fun selectFrame(index: Int) {
        if (index < 0 || index >= state.value.timeline.frames.size) return
        
        // Save current frame state before switching
        saveFrameState()
        
        _state.update { it.copy(
            timeline = it.timeline.copy(currentFrameIndex = index)
        )}
        
        // Load target frame state
        loadFrameState(index)
    }
    
    fun toggleKeyframe() {
        val index = state.value.timeline.currentFrameIndex
        val frames = state.value.timeline.frames.toMutableList()
        if (index in frames.indices) {
            frames[index] = frames[index].copy(isKeyframe = !frames[index].isKeyframe)
            _state.update { it.copy(
                timeline = it.timeline.copy(frames = frames)
            )}
        }
    }
    
    fun setFPS(fps: Int) {
        _state.update { it.copy(
            timeline = it.timeline.copy(fps = fps.coerceIn(1, 60))
        )}
    }
    
    fun togglePlayback() {
        _state.update { it.copy(
            timeline = it.timeline.copy(isPlaying = !it.timeline.isPlaying)
        )}
    }
    
    private fun saveFrameState() {
        // Save current layer states to current frame
        val index = state.value.timeline.currentFrameIndex
        val frames = state.value.timeline.frames.toMutableList()
        
        if (index in frames.indices) {
            val layerStates = state.value.layers.associate { layer ->
                layer.id to LayerFrameState(
                    visible = layer.isVisible,
                    opacity = layer.opacity
                )
            }
            frames[index] = frames[index].copy(layerStates = layerStates)
            _state.update { it.copy(
                timeline = it.timeline.copy(frames = frames)
            )}
        }
    }
    
    private fun loadFrameState(index: Int) {
        val frame = state.value.timeline.frames.getOrNull(index) ?: return
        
        // Restore layer states
        frame.layerStates.forEach { (layerId, layerState) ->
            _state.update { state ->
                state.copy(
                    layers = state.layers.map { layer ->
                        if (layer.id == layerId) {
                            layer.copy(
                                isVisible = layerState.visible,
                                opacity = layerState.opacity
                            )
                        } else layer
                    }
                )
            }
        }
    }
    
    // ===== Onion Skin =====
    fun setOnionSkin(config: OnionSkinState) {
        _state.update { it.copy(onionSkin = config) }
    }
    
    // ===== Panel Management =====
    fun togglePanel(panelName: String) {
        val panels = state.value.panels
        val newPanels = when (panelName) {
            "brushes" -> panels.copy(brushes = panels.brushes.copy(isOpen = !panels.brushes.isOpen))
            "layers" -> panels.copy(layers = panels.layers.copy(isOpen = !panels.layers.isOpen))
            "timeline" -> panels.copy(timeline = panels.timeline.copy(isOpen = !panels.timeline.isOpen))
            "gallery" -> panels.copy(gallery = panels.gallery.copy(isOpen = !panels.gallery.isOpen))
            "settings" -> panels.copy(settings = panels.settings.copy(isOpen = !panels.settings.isOpen))
            "onion" -> panels.copy(onionSkin = panels.onionSkin.copy(isOpen = !panels.onionSkin.isOpen))
            "symmetry" -> panels.copy(symmetry = panels.symmetry.copy(isOpen = !panels.symmetry.isOpen))
            else -> panels
        }
        _state.update { it.copy(panels = newPanels) }
    }
    
    fun setPanelOffset(panelName: String, x: Float, y: Float) {
        val panels = state.value.panels
        val newPanels = when (panelName) {
            "brushes" -> panels.copy(brushes = panels.brushes.copy(offsetX = x, offsetY = y))
            "layers" -> panels.copy(layers = panels.layers.copy(offsetX = x, offsetY = y))
            "timeline" -> panels.copy(timeline = panels.timeline.copy(offsetX = x, offsetY = y))
            "gallery" -> panels.copy(gallery = panels.gallery.copy(offsetX = x, offsetY = y))
            "settings" -> panels.copy(settings = panels.settings.copy(offsetX = x, offsetY = y))
            "onion" -> panels.copy(onionSkin = panels.onionSkin.copy(offsetX = x, offsetY = y))
            "symmetry" -> panels.copy(symmetry = panels.symmetry.copy(offsetX = x, offsetY = y))
            else -> panels
        }
        _state.update { it.copy(panels = newPanels) }
    }
    
    // ===== Settings =====
    fun setSetting(key: String, value: Any) {
        val settings = state.value.settings
        val newSettings = when (key) {
            "palmRejection" -> settings.copy(palmRejection = value as Boolean)
            "stylusOnly" -> settings.copy(stylusOnly = value as Boolean)
            "lowLatency" -> settings.copy(lowLatency = value as Boolean)
            "gestureUndo" -> settings.copy(gestureUndo = value as Boolean)
            "gestureRedo" -> settings.copy(gestureRedo = value as Boolean)
            "gestureFullscreen" -> settings.copy(gestureFullscreen = value as Boolean)
            "autoSave" -> settings.copy(autoSave = value as Boolean)
            else -> settings
        }
        _state.update { it.copy(settings = newSettings) }
    }
    
    // ===== Canvas Operations =====
    fun newCanvas() {
        viewModelScope.launch {
            saveToGallery()
            
            _state.update { DrawingState.initial() }
            undoStack.clear()
            redoStack.clear()
            
            // Add initial frame
            addFrame()
        }
    }
    
    fun saveToGallery() {
        viewModelScope.launch {
            val currentState = state.value
            val metadata = CanvasMetadata(
                id = UUID.randomUUID().toString(),
                name = "Canvas ${currentState.gallery.size + 1}",
                width = currentState.canvasWidth,
                height = currentState.canvasHeight,
                fps = currentState.timeline.fps,
                layerCount = currentState.layers.size,
                frameCount = currentState.timeline.frames.size,
                lastModified = System.currentTimeMillis()
            )
            
            _state.update { it.copy(gallery = it.gallery + metadata) }
            
            // Save to repository
            repository.saveCanvas(currentState)
        }
    }
    
    private fun loadSavedState() {
        viewModelScope.launch {
            try {
                val savedState = repository.loadLastCanvas()
                if (savedState != null) {
                    _state.update { savedState }
                }
            } catch (e: Exception) {
                // Start with fresh state
            }
        }
    }
    
    // ===== Color Utilities =====
    private fun generateLayerColor(): Color {
        val colors = listOf(
            Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1),
            Color(0xFF96CEB4), Color(0xFFFFEAA7), Color(0xFFDDA0DD),
            Color(0xFF98D8C8), Color(0xFFF7DC6F), Color(0xFFBB8FCE),
            Color(0xFF85C1E9), Color(0xFF82E0AA), Color(0xFFF8C471)
        )
        return colors[state.value.layers.size % colors.size]
    }
}
