package com.brine.composepod.core

import com.brine.composepod.async.AsyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A provider that handles Kotlin Flows and exposes them as AsyncState.
 * Automatically handles flow collection and lifecycle management.
 */
open class StreamProvider<T>(
    name: String? = null,
    internal val createFn: (ProviderRef) -> kotlinx.coroutines.flow.Flow<T>
) : ProviderBase<StateFlow<AsyncState<T>>>(name) {

    override fun create(ref: ProviderRef): StateFlow<AsyncState<T>> {
        @Suppress("UNCHECKED_CAST")
        val state = (ref.currentValue as? MutableStateFlow<AsyncState<T>>)
            ?: MutableStateFlow<AsyncState<T>>(AsyncState.Loading())
        
        state.value = AsyncState.Loading()
        
        val flow = createFn(ref)
        
        ref.coroutineScope.launch {
            try {
                flow.collect { value ->
                    state.value = AsyncState.Success(value)
                }
            } catch (e: Exception) {
                state.value = AsyncState.Error(e)
            }
        }
        
        return state
    }

    companion object
}

/**
 * Creates an auto-dispose wrapper for a StreamProvider while preserving its type.
 */
fun <V> StreamProvider<V>.autoDispose(): StreamProvider<V> {
    val origin = this
    val fn: (ProviderRef) -> kotlinx.coroutines.flow.Flow<V> = { ref -> origin.createFn(ref) }
    return object : StreamProvider<V>(
        origin.name?.let { "$it-autoDispose" },
        fn
    ), AutoDisposeProvider<StateFlow<AsyncState<V>>> {
        override val origin: ProviderBase<StateFlow<AsyncState<V>>> = origin
    }
}

/**
 * Creates a [StreamProvider].
 */
fun <T> streamProvider(
    name: String? = null,
    create: (ProviderRef) -> kotlinx.coroutines.flow.Flow<T>
): StreamProvider<T> {
    return StreamProvider(name, create)
}
