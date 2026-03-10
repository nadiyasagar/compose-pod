package com.brine.composepod.core

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A provider that holds simple mutable state.
 * Returns a [MutableStateFlow].
 */
class StateProvider<T>(
    name: String? = null,
    private val createFn: (ProviderRef) -> T
) : ProviderBase<MutableStateFlow<T>>(name) {
    override fun create(ref: ProviderRef): MutableStateFlow<T> {
        return MutableStateFlow(createFn(ref))
    }
}

/**
 * Creates a [StateProvider].
 */
fun <T> stateProvider(name: String? = null, create: (ProviderRef) -> T): StateProvider<T> {
    return StateProvider(name, create)
}
