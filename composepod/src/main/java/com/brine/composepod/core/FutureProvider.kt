package com.brine.composepod.core

import com.brine.composepod.async.AsyncState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * A provider that handles asynchronous operations and returns an AsyncState.
 * Automatically manages Loading, Success, and Error states.
 */
open class FutureProvider<T>(
    name: String? = null,
    internal val createFn: suspend (ProviderRef) -> T
) : ProviderBase<StateFlow<AsyncState<T>>>(name) {

    private val activeJobs = ConcurrentHashMap<ProviderRef, Job>()

    override fun create(ref: ProviderRef): StateFlow<AsyncState<T>> {
        @Suppress("UNCHECKED_CAST")
        val state = (ref.currentValue as? MutableStateFlow<AsyncState<T>>)
            ?: MutableStateFlow<AsyncState<T>>(AsyncState.Loading())
        
        // Cancel any existing job for this node (on refresh)
        activeJobs[ref]?.cancel()
        
        // Start new job
        state.value = AsyncState.Loading()
        
        val job = ref.coroutineScope.launch {
            try {
                val result = createFn(ref)
                state.value = AsyncState.Success(result)
            } catch (e: Exception) {
                state.value = AsyncState.Error(e)
            } finally {
                // Remove self from map when active work is done
                activeJobs.remove(ref)
            }
        }
        
        activeJobs[ref] = job
        
        // Ensure clean-up when the provider itself is disposed
        ref.onDispose {
            activeJobs.remove(ref)?.cancel()
        }
        
        return state
    }

    companion object
}

/**
 * Creates an auto-dispose wrapper for a FutureProvider while preserving its type.
 */
fun <V> FutureProvider<V>.autoDispose(): FutureProvider<V> {
    val origin = this
    val fn: suspend (ProviderRef) -> V = { ref -> origin.createFn(ref) }
    return object : FutureProvider<V>(
        origin.name?.let { "$it-autoDispose" },
        fn
    ), AutoDisposeProvider<StateFlow<AsyncState<V>>> {
        override val origin: ProviderBase<StateFlow<AsyncState<V>>> = origin
    }
}

/**
 * Creates a [FutureProvider].
 */
fun <T> futureProvider(name: String? = null, create: suspend (ProviderRef) -> T): FutureProvider<T> {
    return FutureProvider(name, create)
}
