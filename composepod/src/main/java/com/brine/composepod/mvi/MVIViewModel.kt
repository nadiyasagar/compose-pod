package com.brine.composepod.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Coroutine-backed ViewModel acting as a MVI container for intents and state.
 * Uses [StateNotifier] under the hood, making it fully compatible with ComposePod providers.
 */
abstract class MVIViewModel<State : UiState, Intent : UiIntent>(
    initialState: State,
    private val reducer: Reducer<State, Intent>? = null,
    scope: CoroutineScope? = null
) : StateNotifier<State>(initialState) {

    // Tied to provider lifecycle if scope is provided, otherwise creates its own
    protected val viewModelScope = scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /**
     * Process a UI intent.
     * Can be overridden to handle side-effects and complex flows before/instead of state reduction.
     */
    open fun processIntent(intent: Intent) {
        reducer?.let {
            state = it.reduce(state, intent)
        }
    }
    
    /**
     * Utility for executing asynchronous tasks in the viewmodel.
     */
    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            block()
        }
    }
}
