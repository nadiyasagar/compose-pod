package com.brine.composepod.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A computed provider that automatically re-computes when its dependencies change.
 * Similar to Riverpod's computed providers.
 *
 * Usage:
 * ```kotlin
 * val counterProvider = stateProvider { 0 }
 *
 * val doubledProvider = computedProvider { ref ->
 *     val count = ref.watch(counterProvider)
 *     count * 2
 * }
 * ```
 */
class ComputedProvider<T>(
    name: String? = null,
    private val compute: (ComputedProviderRef) -> T
) : ProviderBase<StateFlow<T>>(name) {

    override fun create(ref: ProviderRef): StateFlow<T> {
        val state = MutableStateFlow(compute(ComputedProviderRef(ref, this)))
        return state.asStateFlow()
    }
}

/**
 * Reference for computed providers that allows watching dependencies.
 */
class ComputedProviderRef(
    private val providerRef: ProviderRef,
    private val computedProvider: ComputedProvider<*>
) {
    /**
     * Watches a provider and establishes a dependency relationship.
     * When the watched provider changes, this computed provider will re-compute.
     */
    fun <T> watch(provider: ProviderBase<T>): T {
        return providerRef.watch(provider)
    }

    /**
     * Reads a provider without establishing a dependency.
     */
    fun <T> read(provider: ProviderBase<T>): T {
        return providerRef.read(provider)
    }
}

/**
 * Creates a computed provider that derives its value from other providers.
 *
 * @param name Optional name for debugging
 * @param compute Lambda that computes the value. Use `ref.watch()` to track dependencies.
 */
fun <T> computedProvider(
    name: String? = null,
    compute: (ComputedProviderRef) -> T
): ComputedProvider<T> {
    return ComputedProvider(name, compute)
}
