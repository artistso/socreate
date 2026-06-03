package com.socreate.app.core.model

/**
 * MVI (Model-View-Intent) Architecture Foundation.
 *
 * Intent → Reducer → State → View
 *
 * This provides a unidirectional data flow for predictable state management.
 */

/**
 * Base interface for all MVI Intents (user actions / system events).
 */
interface MviIntent

/**
 * Base interface for all MVI States (screen state snapshots).
 */
interface MviState

/**
 * Base interface for single-shot side effects (navigation, toasts, etc.).
 */
interface MviEffect

/**
 * Represents the result of processing an intent against the current state.
 */
data class StateTransition<out S : MviState>(
    val newState: S,
    val effects: List<MviEffect> = emptyList()
)

/**
 * Pure function that reduces an intent against the current state.
 * No side effects — just state transformation.
 */
fun interface Reducer<S : MviState, I : MviIntent> {
    fun reduce(currentState: S, intent: I): StateTransition<S>
}
