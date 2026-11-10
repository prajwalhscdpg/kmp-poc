package com.cdpg.dhruva.core.common

import kotlin.coroutines.cancellation.CancellationException

/**
 * Outcome of a repository or use-case call. Same idea as Dhruva Android's `DanAiaResult`, minus
 * `Loading`: loading is screen state, so it lives in each UiState instead.
 */
sealed interface DanAiaResult<out T> {
    data class Success<T>(val data: T) : DanAiaResult<T>
    data class Error(val error: DanAiaError) : DanAiaResult<Nothing>
}

inline fun <T, R> DanAiaResult<T>.map(transform: (T) -> R): DanAiaResult<R> = when (this) {
    is DanAiaResult.Success -> DanAiaResult.Success(transform(data))
    is DanAiaResult.Error -> this
}

inline fun <T> DanAiaResult<T>.onSuccess(action: (T) -> Unit): DanAiaResult<T> {
    if (this is DanAiaResult.Success) action(data)
    return this
}

inline fun <T> DanAiaResult<T>.onError(action: (DanAiaError) -> Unit): DanAiaResult<T> {
    if (this is DanAiaResult.Error) action(error)
    return this
}

/** Like runCatching, but never swallows coroutine cancellation (Dhruva engineering rule). */
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Throwable) {
    Result.failure(e)
}
