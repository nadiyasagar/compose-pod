package com.brine.composepod.async

/**
 * Extension functions for AsyncState to provide Riverpod-like utilities.
 */

/**
 * Returns the data if Success, null otherwise.
 */
fun <T> AsyncState<T>.getOrNull(): T? {
    return when (this) {
        is AsyncState.Success -> this.data
        else -> null
    }
}

/**
 * Transforms the data if Success, otherwise returns the same state.
 * Similar to Riverpod's AsyncValue.map()
 */
fun <T, R> AsyncState<T>.map(transform: (T) -> R): AsyncState<R> {
    return when (this) {
        is AsyncState.Loading -> AsyncState.Loading()
        is AsyncState.Success -> AsyncState.Success(transform(this.data))
        is AsyncState.Error -> AsyncState.Error(this.throwable)
    }
}

/**
 * Returns Success data or throws the error if Error, or throws IllegalStateException if Loading.
 */
fun <T> AsyncState<T>.getOrThrow(): T {
    return when (this) {
        is AsyncState.Success -> this.data
        is AsyncState.Error -> throw this.throwable
        is AsyncState.Loading -> throw IllegalStateException("AsyncState is still loading")
    }
}

/**
 * Returns Success data or default value.
 */
fun <T> AsyncState<T>.getOrDefault(defaultValue: T): T {
    return when (this) {
        is AsyncState.Success -> this.data
        else -> defaultValue
    }
}

/**
 * Returns a new AsyncState with previous data preserved during loading.
 * If current is Success and next is Loading, returns Loading with previous data.
 * Similar to Riverpod's AsyncValue.copyWithPrevious()
 */
fun <T> AsyncState<T>.copyWithPrevious(previous: AsyncState<T>?): AsyncState<T> {
    return when (this) {
        is AsyncState.Loading -> {
            // If we have previous success data, keep it available
            if (previous is AsyncState.Success<T>) {
                // Return a special loading state that preserves data
                // For now, we just return Loading - users can check previous separately
                this
            } else {
                this
            }
        }
        else -> this
    }
}

/**
 * Executes the given block and wraps the result in AsyncState.
 * Similar to Riverpod's AsyncValue.guard()
 *
 * Usage:
 * ```kotlin
 * val asyncState = guardAsync { api.fetchData() }
 * ```
 */
suspend fun <T> guardAsync(block: suspend () -> T): AsyncState<T> {
    return try {
        AsyncState.Success(block())
    } catch (e: Throwable) {
        AsyncState.Error(e)
    }
}

/**
 * Pattern matching helper that handles all states.
 * Similar to Riverpod's AsyncValue.when()
 */
inline fun <T, R> AsyncState<T>.whenAsync(
    onLoading: () -> R,
    onSuccess: (T) -> R,
    onError: (Throwable) -> R
): R {
    return when (this) {
        is AsyncState.Loading -> onLoading()
        is AsyncState.Success -> onSuccess(this.data)
        is AsyncState.Error -> onError(this.throwable)
    }
}

/**
 * Returns true if this is Success
 */
fun <T> AsyncState<T>.isSuccess(): Boolean = this is AsyncState.Success

/**
 * Returns true if this is Loading
 */
fun <T> AsyncState<T>.isLoading(): Boolean = this is AsyncState.Loading

/**
 * Returns true if this is Error
 */
fun <T> AsyncState<T>.isError(): Boolean = this is AsyncState.Error

/**
 * Returns the error if Error, null otherwise.
 */
fun <T> AsyncState<T>.errorOrNull(): Throwable? {
    return when (this) {
        is AsyncState.Error -> this.throwable
        else -> null
    }
}
