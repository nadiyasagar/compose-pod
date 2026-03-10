package com.brine.composepod.async

sealed class AsyncState<T> {
    class Loading<T> : AsyncState<T>()
    data class Success<T>(val data: T) : AsyncState<T>()
    data class Error<T>(val throwable: Throwable) : AsyncState<T>()
}
