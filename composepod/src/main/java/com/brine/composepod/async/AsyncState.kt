package com.brine.composepod.async

import androidx.compose.runtime.Composable

sealed class AsyncState<T> {
    class Loading<T> : AsyncState<T>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error<T>(val throwable: Throwable) : AsyncState<T>()

    val isLoading: Boolean get() = this is Loading
    val hasValue: Boolean get() = this is Success
    val hasError: Boolean get() = this is Error

    fun valueOrNull(): T? = (this as? Success)?.data
    fun errorOrNull(): Throwable? = (this as? Error)?.throwable

    companion object {
        fun <T> loading(): AsyncState<T> = Loading()
        fun <T> success(data: T): AsyncState<T> = Success(data)
        fun <T> error(throwable: Throwable): AsyncState<T> = Error(throwable)
    }
}

/**
 * Extension to handle AsyncState in Compose.
 */
@Composable
fun <T, R> AsyncState<T>.`when`(
    data: @Composable (T) -> R,
    loading: @Composable () -> R,
    error: @Composable (Throwable) -> R
): R {
    return when (this) {
        is AsyncState.Loading -> loading()
        is AsyncState.Success -> data(this.data)
        is AsyncState.Error -> error(this.throwable)
    }
}
