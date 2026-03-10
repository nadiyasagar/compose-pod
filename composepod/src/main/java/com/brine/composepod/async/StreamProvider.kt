package com.brine.composepod.async

import com.brine.composepod.core.ProviderBase
import com.brine.composepod.core.ProviderRef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * A provider that collects a [Flow] and exposes its latest value as [AsyncState].
 */
class StreamProvider<T>(
    name: String? = null,
    private val createFn: (ProviderRef) -> Flow<T>
) : ProviderBase<StateFlow<AsyncState<T>>>(name) {

    override fun create(ref: ProviderRef): StateFlow<AsyncState<T>> {
        val state = MutableStateFlow<AsyncState<T>>(AsyncState.Loading())
        val flow = createFn(ref)
        
        ref.coroutineScope.launch {
            flow.catch { e ->
                state.value = AsyncState.Error(e)
            }.collect { data ->
                state.value = AsyncState.Success(data)
            }
        }
        
        return state
    }
}

/**
 * Creates a [StreamProvider].
 */
fun <T> streamProvider(name: String? = null, create: (ProviderRef) -> Flow<T>): StreamProvider<T> {
    return StreamProvider(name, create)
}
