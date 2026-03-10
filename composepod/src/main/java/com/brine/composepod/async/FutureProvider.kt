package com.brine.composepod.async

import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderRef
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A provider that executes a suspend function and exposes its result as [AsyncState].
 */
class FutureProvider<T>(
    name: String? = null,
    private val createFn: suspend (ProviderRef) -> T
) : ProviderBase<StateFlow<AsyncState<T>>>(name) {

    override fun create(ref: ProviderRef): StateFlow<AsyncState<T>> {
        val state = MutableStateFlow<AsyncState<T>>(AsyncState.Loading())
        
        ref.coroutineScope.launch {
            try {
                val result = createFn(ref)
                state.value = AsyncState.Success(result)
            } catch (e: Exception) {
                state.value = AsyncState.Error(e)
            }
        }
        
        return state
    }
}

/**
 * Creates a [FutureProvider].
 */
fun <T> futureProvider(name: String? = null, create: suspend (ProviderRef) -> T): FutureProvider<T> {
    return FutureProvider(name, create)
}
