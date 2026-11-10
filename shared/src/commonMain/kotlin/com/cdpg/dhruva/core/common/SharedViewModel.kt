package com.cdpg.dhruva.core.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Base class for the shared ViewModels. Holds the state logic both apps used to duplicate;
 * the native UI only renders state and forwards user actions.
 *
 * Lifecycle: Android wraps it in a Jetpack ViewModel that calls [clear] in onCleared
 * (androidApp `rememberSharedViewModel`); iOS calls [clear] when the SwiftUI owner deinits.
 */
abstract class SharedViewModel {

    protected val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Cancels everything the ViewModel started. Safe to call more than once. */
    open fun clear() {
        scope.cancel()
    }
}
