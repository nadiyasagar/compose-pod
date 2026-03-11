package com.brine.composepod.core

import com.brine.composepod.mvi.StateNotifier
import com.brine.composepod.mvi.StateNotifierProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface given to providers allowing them to read other providers.
 * Also allows registering dispose callbacks.
 */
interface ProviderRef {
    
    /**
     * A coroutine scope tied to the lifecycle of this provider.
     * It is cancelled when the provider is disposed.
     */
    val coroutineScope: CoroutineScope
    /**
     * Reads the current value of another provider WITHOUT listening to its changes.
     */
    fun <T> read(provider: ProviderBase<T>): T
    
    /**
     * Watches another provider's value. If the watched provider updates,
     * this provider will be invalidated and rebuilt.
     */
    fun <T> watch(provider: ProviderBase<T>): T
    
    /**
     * Forcefully invalidates a provider from the container.
     */
    fun invalidate(provider: ProviderBase<*>)
    
    /**
     * Reads a specific property from a provider's state using a selector.
     */
    fun <T, R> select(provider: ProviderBase<out StateFlow<T>>, selector: (T) -> R): R

    /**
     * Reads a specific property from a StateNotifierProvider's state using a selector.
     */
    fun <Notifier : StateNotifier<S>, S, R> select(provider: StateNotifierProvider<Notifier, S>, selector: (S) -> R): R
    
    /**
     * Registers a callback to be called when this provider is disposed.
     */
    fun onDispose(callback: () -> Unit)
}
