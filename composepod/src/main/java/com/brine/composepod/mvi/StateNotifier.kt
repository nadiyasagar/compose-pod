package com.brine.composepod.mvi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base class for managing complex business logic and state.
 * Inspired by StateNotifier in Riverpod.
 */
abstract class StateNotifier<T>(initialState: T) {
    private val _state = MutableStateFlow(initialState)
    val stateFlow: StateFlow<T> = _state.asStateFlow()

    protected var state: T
        get() = _state.value
        set(value) {
            _state.value = value
        }
}
