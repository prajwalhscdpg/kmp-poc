package com.cdpg.dhruva.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/*
 * Swift can't collect a Kotlin Flow directly. These small wrappers expose a callback API that
 * SwiftUI can observe (see iosApp/iosApp/Bridge/KmpState.swift), without extra Gradle plugins.
 * They are classes (not interfaces) so the generic type parameter survives into Swift.
 * Android ignores them and collects [flow] with collectAsStateWithLifecycle().
 */

/** Returned by watch(); call [close] when the observer goes away. */
fun interface WatchHandle {
    fun close()
}

class CStateFlow<T : Any>(val flow: StateFlow<T>) {

    /** Current value, readable synchronously from Swift. */
    val value: T get() = flow.value

    /** Calls [onEach] on the main thread with the current value and every change. */
    fun watch(onEach: (T) -> Unit): WatchHandle = watchOnMain(flow, onEach)
}

class CFlow<T : Any>(val flow: Flow<T>) {

    /** Calls [onEach] on the main thread for every emission (one-off events). */
    fun watch(onEach: (T) -> Unit): WatchHandle = watchOnMain(flow, onEach)
}

fun <T : Any> StateFlow<T>.asCStateFlow(): CStateFlow<T> = CStateFlow(this)

fun <T : Any> Flow<T>.asCFlow(): CFlow<T> = CFlow(this)

private fun <T> watchOnMain(flow: Flow<T>, onEach: (T) -> Unit): WatchHandle {
    val scope: CoroutineScope = MainScope()
    scope.launch(Dispatchers.Main.immediate) { flow.collect { onEach(it) } }
    return WatchHandle { scope.cancel() }
}
