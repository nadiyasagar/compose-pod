package com.brine.composepod.mvi

import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderRef

/**
 * A provider that creates and exposes a [StateNotifier].
 */
class StateNotifierProvider<Notifier : StateNotifier<State>, State>(
    name: String? = null,
    private val createFn: (ProviderRef) -> Notifier
) : ProviderBase<Notifier>(name) {

    override fun create(ref: ProviderRef): Notifier {
        return createFn(ref)
    }
}

/**
 * Creates a [StateNotifierProvider].
 */
fun <Notifier : StateNotifier<State>, State> stateNotifierProvider(
    name: String? = null,
    create: (ProviderRef) -> Notifier
): StateNotifierProvider<Notifier, State> {
    return StateNotifierProvider(name, create)
}
