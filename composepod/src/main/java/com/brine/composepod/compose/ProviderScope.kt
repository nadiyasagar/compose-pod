package com.brine.composepod.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.brine.composepod.core.ProviderContainer

/**
 * Composition local that holds the [ProviderContainer] for the tree.
 */
val LocalProviderContainer = staticCompositionLocalOf<ProviderContainer> {
    error("No ProviderContainer provided. Wrap your component in ProviderScope.")
}

/**
 * A scope that provides the [ProviderContainer] to all its descendants.
 * This should typically wrap your entire app.
 */
@Composable
fun ProviderScope(
    container: ProviderContainer = remember { ProviderContainer() },
    content: @Composable () -> Unit
) {
    // Dispose resources when ProviderScope leaves the composition
    DisposableEffect(container) {
        onDispose {
            container.dispose()
        }
    }

    CompositionLocalProvider(
        LocalProviderContainer provides container
    ) {
        content()
    }
}
