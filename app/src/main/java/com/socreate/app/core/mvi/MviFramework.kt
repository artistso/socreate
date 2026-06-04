package com.socreate.app.core.mvi

import androidx.compose.runtime.Stable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * MVI (Model-View-Intent) Architecture Base Classes
 * Provides unidirectional data flow for SoCreate
 */

// ===== Intent =====
interface Intent

// ===== State =====
interface State {
    fun copy(): State
}

// ===== Event =====
interface Event

// ===== Reducer =====
interface Reducer<S : State> {
    fun reduce(state: S, intent: Intent): S
}

// ===== ViewModel Base =====
@Stable
abstract class MviViewModel<S : State>(
    initialState: S,
    private val reducer: Reducer<S>
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()
    
    private val _events = Channel<Event>()
    val events: Flow<Event> = _events.receiveAsFlow()
    
    fun processIntent(intent: Intent) {
        val currentState = _state.value
        val newState = reducer.reduce(currentState, intent)
        _state.value = newState
    }
    
    protected fun sendEvent(event: Event) {
        _events.trySend(event)
    }
    
    protected fun updateState(update: S.() -> S) {
        _state.value = _state.value.update()
    }
}

// ===== Drawing Intent Types =====
sealed class DrawingIntent : Intent {
    data class SetBrushType(val type: com.socreate.app.core.model.BrushType) : DrawingIntent()
    data class SetBrushSize(val size: Float) : DrawingIntent()
    data class SetBrushOpacity(val opacity: Float) : DrawingIntent()
    data class SetBrushHardness(val hardness: Float) : DrawingIntent()
    data class SetBrushColor(val color: androidx.compose.ui.graphics.Color) : DrawingIntent()
    data class SetSymmetry(val mode: com.socreate.app.core.model.SymmetryMode) : DrawingIntent()
    data class SetShapeDetection(val enabled: Boolean) : DrawingIntent()
    
    data class AddLayer(val name: String) : DrawingIntent()
    data class SelectLayer(val id: String) : DrawingIntent()
    data class DeleteLayer(val id: String) : DrawingIntent()
    data class ToggleLayerVisibility(val id: String) : DrawingIntent()
    data class SetLayerOpacity(val id: String, val opacity: Float) : DrawingIntent()
    
    data class StartStroke(val point: androidx.compose.ui.geometry.Offset) : DrawingIntent()
    data class MoveStroke(val point: androidx.compose.ui.geometry.Offset, val pressure: Float) : DrawingIntent()
    object EndStroke : DrawingIntent()
    
    object Undo : DrawingIntent()
    object Redo : DrawingIntent()
    
    data class AddFrame(val index: Int) : DrawingIntent()
    data class SelectFrame(val index: Int) : DrawingIntent()
    data class ToggleKeyframe(val index: Int) : DrawingIntent()
    data class SetFPS(val fps: Int) : DrawingIntent()
    object TogglePlayback : DrawingIntent()
    
    data class SetOnionSkin(val config: com.socreate.app.core.model.OnionSkinState) : DrawingIntent()
    
    data class TogglePanel(val panelName: String) : DrawingIntent()
    data class SetPanelOffset(val panelName: String, val x: Float, val y: Float) : DrawingIntent()
    
    data class SetSetting(val key: String, val value: Any) : DrawingIntent()
    object NewCanvas : DrawingIntent()
    object SaveToGallery : DrawingIntent()
}

// ===== Drawing Events =====
sealed class DrawingEvent : Event {
    object CanvasSaved : DrawingEvent()
    object FrameAdded : DrawingEvent()
    data class Error(val message: String) : DrawingEvent()
    data class Toast(val message: String) : DrawingEvent()
}
