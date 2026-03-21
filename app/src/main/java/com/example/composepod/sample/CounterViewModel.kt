package com.example.composepod.sample

import com.brine.composepod.mvi.MVIViewModel
import com.brine.composepod.mvi.StateNotifierProvider
import com.brine.composepod.mvi.UiIntent
import com.brine.composepod.mvi.UiState
import com.brine.composepod.mvi.stateNotifierProvider

// 1. Define State
data class CounterState(val count: Int = 0) : UiState

// 2. Define Intents
sealed class CounterIntent : UiIntent {
    object Increment : CounterIntent()
    object Decrement : CounterIntent()
}

// 3. Define the ViewModel (StateNotifier)
class CounterViewModel : MVIViewModel<CounterState, CounterIntent>(
    initialState = CounterState()
) {
    override fun processIntent(intent: CounterIntent) {
        when (intent) {
            is CounterIntent.Increment -> state = state.copy(count = state.count + 1)
            is CounterIntent.Decrement -> state = state.copy(count = state.count - 1)
        }
    }
}

// 4. Create a Provider
val counterProvider: StateNotifierProvider<CounterViewModel, CounterState> =
    stateNotifierProvider {
        CounterViewModel()
    }
