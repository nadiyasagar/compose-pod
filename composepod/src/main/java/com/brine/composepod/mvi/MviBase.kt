package com.brine.composepod.mvi

/**
 * Marker interface for UI State.
 */
interface UiState

/**
 * Marker interface for UI Intent (Action/Event).
 */
interface UiIntent

/**
 * Defines a reducer to evolve state based on an intent.
 */
interface Reducer<State : UiState, Intent : UiIntent> {
    fun reduce(state: State, intent: Intent): State
}
