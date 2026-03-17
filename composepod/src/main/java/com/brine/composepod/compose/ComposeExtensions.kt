package com.brine.composepod.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.mutableStateOf
import com.brine.composepod.core.NotifierProviderSelector
import com.brine.composepod.core.Provider
import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderSelector
import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Watches a provider that exposes a [StateFlow] (such as [StateProvider], [FutureProvider], [StreamProvider]).
 * Returns a Compose [State] that triggers recomposition when the flow emits deeply.
 */
@Composable
fun <T> watchProvider(provider: ProviderBase<out StateFlow<T>>): State<T> {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    val currentFlowState = remember(container, provider) {
        mutableStateOf(container.read(provider))
    }

    DisposableEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()
        
        val listener: (Any?) -> Unit = { newValue ->
            if (newValue is StateFlow<*>) {
                @Suppress("UNCHECKED_CAST")
                currentFlowState.value = newValue as StateFlow<T>
            }
        }
        node.addOnValueChangeListener(listener)
        
        onDispose { 
            node.removeOnValueChangeListener(listener)
            node.removeListener() 
        }
    }

    return currentFlowState.value.collectAsState()
}

/**
 * Watches a [StateNotifierProvider]. Returns a Compose [State] of its inner state.
 */
@Composable
fun <Notifier : StateNotifier<S>, S> watchProvider(
    provider: StateNotifierProvider<Notifier, S>
): State<S> {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    val currentNotifierState = remember(container, provider) {
        mutableStateOf(container.read(provider))
    }

    DisposableEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()
        
        val listener: (Notifier) -> Unit = { newValue ->
            currentNotifierState.value = newValue
        }
        node.addOnValueChangeListener(listener)
        
        onDispose { 
            node.removeOnValueChangeListener(listener)
            node.removeListener() 
        }
    }

    return currentNotifierState.value.stateFlow.collectAsState()
}

/**
 * Watches a [ProviderSelector]. Returns a Compose [State] of the selected value.
 * Triggers recomposition only when the projected value changes.
 */
@Composable
fun <T, R> watchProvider(selectorObj: ProviderSelector<T, R>): State<R> {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    DisposableEffect(container, selectorObj.provider) {
        val node = container.getNode(selectorObj.provider)
        node.addListener()
        onDispose { node.removeListener() }
    }

    val flow = container.read(selectorObj.provider)
    
    // We remember the mapped flow to avoid recreating it on every recomposition
    val mappedFlow = remember(flow, selectorObj.selector) {
        flow.map(selectorObj.selector).distinctUntilChanged()
    }
    
    // We provide an initial value for collectAsState
    val initialValue = remember(flow, selectorObj.selector) {
        selectorObj.selector(flow.value)
    }
    
    return mappedFlow.collectAsState(initial = initialValue)
}

/**
 * Watches a [NotifierProviderSelector]. Returns a Compose [State] of the selected value.
 * Triggers recomposition only when the projected value changes.
 */
@Composable
fun <Notifier : StateNotifier<S>, S, R> watchProvider(
    selectorObj: NotifierProviderSelector<Notifier, S, R>
): State<R> {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    DisposableEffect(container, selectorObj.provider) {
        val node = container.getNode(selectorObj.provider)
        node.addListener()
        onDispose { node.removeListener() }
    }

    val notifier = container.read(selectorObj.provider)
    
    val mappedFlow = remember(notifier.stateFlow, selectorObj.selector) {
        notifier.stateFlow.map(selectorObj.selector).distinctUntilChanged()
    }
    
    val initialValue = remember(notifier.stateFlow, selectorObj.selector) {
        selectorObj.selector(notifier.stateFlow.value)
    }
    
    return mappedFlow.collectAsState(initial = initialValue)
}

/**
 * Overload for plain [ProviderBase] which doesn't alter state.
 */
@JvmName("watchProviderPlain")
@Composable
fun <T> watchProvider(provider: ProviderBase<T>): State<T> {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    val state = remember(container, provider) {
        mutableStateOf(container.read(provider))
    }

    DisposableEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()
        
        val listener: (T) -> Unit = { newValue ->
            state.value = newValue
        }
        node.addOnValueChangeListener(listener)
        
        onDispose { 
            node.removeOnValueChangeListener(listener)
            node.removeListener() 
        }
    }

    return state
}

/**
 * Remembers the provider value. Useful for getting access to the value or Notifier
 * to be used inside callbacks such as `onClick` where Composable functions cannot be called.
 */
@Composable
fun <T> rememberProvider(provider: ProviderBase<T>): T {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")

    DisposableEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()
        onDispose { node.removeListener() }
    }

    return remember(container, provider) {
        container.read(provider)
    }
}

/**
 * Utility to read a provider during Composition without watching it.
 * Note: Does not trigger recomposition if the provider state changes.
 */
@Composable
fun <T> readProvider(provider: ProviderBase<T>): T {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")
    return container.read(provider)
}

/**
 * Listens to a provider's changes and invokes a callback.
 * Does not trigger recomposition.
 */
@Composable
fun <T> listenProvider(
    provider: ProviderBase<T>,
    listener: (T?, T) -> Unit
) {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")
    val currentListenerState = rememberUpdatedState(listener)

    DisposableEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()

        var previousValue: T? = null
        val valueListener: (T) -> Unit = { newValue ->
            currentListenerState.value(previousValue, newValue)
            previousValue = newValue
        }

        node.addOnValueChangeListener(valueListener)
        onDispose {
            node.removeOnValueChangeListener(valueListener)
            node.removeListener()
        }
    }
}

/**
 * Listens to a [StateNotifierProvider]'s state changes.
 * Does not trigger recomposition.
 */
@Composable
fun <Notifier : StateNotifier<S>, S> listenProvider(
    provider: StateNotifierProvider<Notifier, S>,
    listener: (S?, S) -> Unit
) {
    val container = LocalProviderContainer.current ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")
    val currentListenerState = rememberUpdatedState(listener)

    androidx.compose.runtime.LaunchedEffect(container, provider) {
        val node = container.getNode(provider)
        node.addListener()
        try {
            val notifier = container.read(provider)
            var previousValue: S? = null
            var firstEmission = true
            
            notifier.stateFlow.collect { newValue ->
                if (firstEmission) {
                    previousValue = newValue
                    firstEmission = false
                } else {
                    currentListenerState.value(previousValue, newValue)
                    previousValue = newValue
                }
            }
        } finally {
            node.removeListener()
        }
    }
}

/**
 * Fluent extension to listen to a [StateNotifierProvider].
 */
@Composable
fun <Notifier : StateNotifier<S>, S> StateNotifierProvider<Notifier, S>.listen(
    listener: (S?, S) -> Unit
) {
    listenProvider(this, listener)
}
