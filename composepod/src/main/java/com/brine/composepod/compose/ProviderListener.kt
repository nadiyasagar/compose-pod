package com.brine.composepod.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.brine.composepod.core.ProviderBase
import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.flow.StateFlow

/**
 * Listens to a provider's state changes and invokes callbacks without triggering recomposition.
 * Similar to Riverpod's ProviderListener widget.
 *
 * Usage:
 * ```kotlin
 * ProviderListener(
 *     provider = counterProvider,
 *     onChange = { count ->
 *         // Handle side effect (e.g., show snackbar, log analytics)
 *         viewModel.logCounterChanged(count)
 *     }
 * ) {
 *     // Child content that doesn't rebuild on provider changes
 *     StaticContent()
 * }
 * ```
 */
@Composable
fun <T> ProviderListener(
    provider: ProviderBase<out StateFlow<T>>,
    onChange: (T) -> Unit,
    content: @Composable () -> Unit
) {
    val container = LocalProviderContainer.current
    val currentOnChange by rememberUpdatedState(onChange)

    DisposableEffect(container, provider) {
        val node = container?.getNode(provider)
        node?.addListener()

        onDispose {
            node?.removeListener()
        }
    }

    // Use LaunchedEffect to collect and invoke callback
    LaunchedEffect(container, provider) {
        container?.read(provider)?.collect { value ->
            currentOnChange(value)
        }
    }

    content()
}

/**
 * Listens to a StateNotifierProvider and invokes callbacks on state changes.
 *
 * Usage:
 * ```kotlin
 * ProviderListener(
 *     provider = notesProvider,
 *     onChange = { state ->
 *         if (state.notes.isEmpty()) {
 *             snackbarHostState.showSnackbar("No notes yet!")
 *         }
 *     }
 * ) {
 *     NotesScreen()
 * }
 * ```
 */
@Composable
fun <Notifier : StateNotifier<S>, S> ProviderListener(
    provider: StateNotifierProvider<Notifier, S>,
    onChange: (S) -> Unit,
    content: @Composable () -> Unit
) {
    val container = LocalProviderContainer.current
    val currentOnChange by rememberUpdatedState(onChange)

    DisposableEffect(container, provider) {
        val node = container?.getNode(provider)
        node?.addListener()

        onDispose {
            node?.removeListener()
        }
    }

    LaunchedEffect(container, provider) {
        container?.read(provider)?.stateFlow?.collect { value ->
            currentOnChange(value)
        }
    }

    content()
}

/**
 * A variant that only triggers onChange when the provider becomes available (initial load).
 * Useful for one-time setup actions.
 */
@Composable
fun <T> ProviderListenerOnce(
    provider: ProviderBase<out StateFlow<T>>,
    onFirstChange: (T) -> Unit,
    content: @Composable () -> Unit
) {
    val container = LocalProviderContainer.current
    val currentOnFirstChange by rememberUpdatedState(onFirstChange)
    val hasTriggered = remember { androidx.compose.runtime.mutableStateOf(false) }

    DisposableEffect(container, provider) {
        val node = container?.getNode(provider)
        node?.addListener()

        onDispose {
            node?.removeListener()
        }
    }

    LaunchedEffect(container, provider) {
        if (!hasTriggered.value) {
            container?.read(provider)?.collect { value ->
                if (!hasTriggered.value) {
                    hasTriggered.value = true
                    currentOnFirstChange(value)
                }
            }
        }
    }

    content()
}
