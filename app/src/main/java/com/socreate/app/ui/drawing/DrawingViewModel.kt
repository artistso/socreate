package com.socreate.app.ui.drawing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.socreate.app.core.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the Drawing screen using MVI architecture.
 *
 * Single source of truth for all drawing state.
 * Receives intents, reduces them to new state, and emits the state
 * for the View to render.
 *
 * ┌──────────┐   Intent    ┌──────────────┐   State    ┌──────────────┐
 * │   View   │ ──────────> │  ViewModel   │ ─────────> │     View     │
 * │ (Canvas) │             │  (Reducer)   │            │  (Renderer)  │
 * └──────────┘             └──────────────┘            └──────────────┘
 *                                 │
 *                           ┌─────┴─────┐
 *                           │   UseCase │
 *                           │ Repository│
 *                           └───────────┘
 */
class DrawingViewModel(application: Application) : AndroidViewModel(application) {

    // State flow (observed by the Activity/Fragment)
    private val _state = MutableStateFlow(DrawingState())
    val state: StateFlow<DrawingState> = _state.asStateFlow()

    // Effects flow (one-shot events like toasts, navigation)
    private val _effects = MutableSharedFlow<DrawingEffect>()
    val effects: SharedFlow<DrawingEffect> = _effects.asSharedFlow()

    // Reducer
    private val reducer = DrawingReducer

    // Intent processing scope
    private val intentScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Intent channel — thread-safe intent queue
    private val intentChannel = Channel<DrawingIntent>(Channel.UNLIMITED)

    init {
        // Start processing intents
        processIntents()

        // Initialize default state
        _state.value = DrawingState(
            canvas = Canvas(),
            layerStack = LayerStack.createDefault(),
            canUndo = false,
            canRedo = false
        )
    }

    /**
     * Accept an intent from the View layer.
     * Thread-safe — can be called from any thread.
     */
    fun acceptIntent(intent: DrawingIntent) {
        intentChannel.trySend(intent)
    }

    /**
     * Process intents sequentially through the reducer.
     */
    private fun processIntents() {
        viewModelScope.launch(Dispatchers.Main) {
            for (intent in intentChannel) {
                val currentState = _state.value
                val transition = reducer.reduce(currentState, intent)

                // Update state
                _state.value = transition.newState

                // Emit side effects
                for (effect in transition.effects.filterIsInstance<DrawingEffect>()) {
                    _effects.emit(effect)
                }

                // Handle async side effects
                handleSideEffects(intent, transition.newState)
            }
        }
    }

    /**
     * Handle side effects that require async work (saving, loading, etc.)
     */
    private fun handleSideEffects(intent: DrawingIntent, state: DrawingState) {
        when (intent) {
            is DrawingIntent.SaveProject -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // TODO: Save project to Room database
                    delay(500) // Simulate save
                    _state.value = state.copy(isSaving = false)
                    _effects.emit(DrawingEffect.ProjectSaved)
                }
            }

            is DrawingIntent.LoadProject -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // TODO: Load project from Room database
                }
            }

            is DrawingIntent.ExportImage -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // TODO: Export to PNG/JPEG/PSD
                    _effects.emit(DrawingEffect.ShowExportDialog("PNG"))
                }
            }

            else -> { /* No side effects */ }
        }
    }

    /**
     * Get the current state synchronously.
     */
    fun getCurrentState(): DrawingState = _state.value

    override fun onCleared() {
        super.onCleared()
        intentScope.cancel()
        intentChannel.close()
    }
}
