package com.brine.composepod.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.brine.composepod.core.Override
import com.brine.composepod.core.ProviderContainer
import com.brine.composepod.core.ProviderObserver

/**
 * Composition local that holds the [ProviderContainer] for the tree.
 */
val LocalProviderContainer = staticCompositionLocalOf<ProviderContainer?> {
    null
}

/**
 * A scope that provides the [ProviderContainer] to all its descendants.
 *
 * Supports nested scopes - child scopes inherit from parent scopes and can add
 * their own overrides. This allows for scoped dependency injection.
 *
 * Usage:
 * ```kotlin
 * // Root scope
 * ProviderScope {
 *     // Child scope with additional overrides
 *     ProviderScope(
 *         overrides = listOf(apiProvider.overrideWith { FakeApi() })
 *     ) {
 *         // This subtree uses FakeApi
 *         SettingsScreen()
 *     }
 * }
 * ```
 */
@Composable
fun ProviderScope(
    overrides: List<Override> = emptyList(),
    observers: List<ProviderObserver> = emptyList(),
    content: @Composable () -> Unit
) {
    // Get parent container if available (for nested scopes)
    val parentContainer = LocalProviderContainer.current

    // Create a new container that inherits from parent if available
    val container = remember(overrides, observers, parentContainer) {
        ProviderContainer(
            parent = parentContainer,
            overrides = overrides, 
            observers = observers
        )
    }

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

/**
 * Creates a nested ProviderScope that inherits from the parent scope.
 * The child scope can override providers from the parent for its subtree.
 */
@Composable
fun NestedProviderScope(
    overrides: List<Override> = emptyList(),
    observers: List<ProviderObserver> = emptyList(),
    content: @Composable () -> Unit
) {
    ProviderScope(
        overrides = overrides,
        observers = observers,
        content = content
    )
}

/**
 * Provides access to the current ProviderContainer.
 * Useful for advanced use cases like manual refresh/invalidate.
 */
@Composable
fun currentProviderContainer(): ProviderContainer {
    return LocalProviderContainer.current
        ?: error("No ProviderContainer provided. Wrap your component in ProviderScope.")
}
