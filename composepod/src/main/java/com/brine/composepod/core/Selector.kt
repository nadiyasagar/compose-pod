package com.brine.composepod.core

import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * A selector for a [ProviderBase] that exposes a [StateFlow].
 */
data class ProviderSelector<T, R>(
    val provider: ProviderBase<out StateFlow<T>>,
    val selector: (T) -> R
)

/**
 * Extension to create a [ProviderSelector] from a [ProviderBase] that exposes a [StateFlow].
 */
fun <T, R> ProviderBase<out StateFlow<T>>.select(selector: (T) -> R): ProviderSelector<T, R> {
    return ProviderSelector(this, selector)
}

/**
 * A selector for a [StateNotifierProvider].
 */
data class NotifierProviderSelector<Notifier : StateNotifier<S>, S, R>(
    val provider: StateNotifierProvider<Notifier, S>,
    val selector: (S) -> R
)

/**
 * Extension to create a [NotifierProviderSelector] from a [StateNotifierProvider].
 */
fun <Notifier : StateNotifier<S>, S, R> StateNotifierProvider<Notifier, S>.select(selector: (S) -> R): NotifierProviderSelector<Notifier, S, R> {
    return NotifierProviderSelector(this, selector)
}
