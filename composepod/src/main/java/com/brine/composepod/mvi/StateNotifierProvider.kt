package com.brine.composepod.mvi

import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderRef

/**
 * A provider that creates and exposes a [StateNotifier].
 */
open class StateNotifierProvider<Notifier : StateNotifier<State>, State>(
    name: String? = null,
    internal val createFn: (ProviderRef) -> Notifier
) : ProviderBase<Notifier>(name) {

    override fun create(ref: ProviderRef): Notifier {
        return createFn(ref)
    }
}

/**
 * Creates an auto-dispose wrapper for a StateNotifierProvider while preserving its type.
 */
fun <Notifier : StateNotifier<S>, S> StateNotifierProvider<Notifier, S>.autoDispose(): StateNotifierProvider<Notifier, S> {
    return object : StateNotifierProvider<Notifier, S>(
        this.name?.let { "$it-autoDispose" },
        { ref -> this@autoDispose.createFn(ref) }
    ), com.brine.composepod.core.AutoDisposeProvider<Notifier> {
        override val origin: com.brine.composepod.core.ProviderBase<Notifier> = this@autoDispose
    }
}

/**
 * Keeps the provider state alive even after all listeners are removed.
 */
fun <Notifier : StateNotifier<S>, S> StateNotifierProvider<Notifier, S>.keepAlive(): StateNotifierProvider<Notifier, S> {
    return object : StateNotifierProvider<Notifier, S>(
        this.name?.let { "$it-keepAlive" },
        { ref -> this@keepAlive.createFn(ref) }
    ) {}
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
