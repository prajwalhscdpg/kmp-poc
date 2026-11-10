package com.cdpg.dhruva.presentation.splash

import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.SharedViewModel
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.session.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SplashDestination {
    /** Signed out: the intro ("Your Address, Simplified.") and then Login. */
    Intro,

    /** Signed in: straight to the tabs. */
    Main,
}

data class SplashUiState(val destination: SplashDestination? = null)

/**
 * Keeps the branded splash (Department of Posts attribution) on screen long enough to read,
 * then routes on the stored session. Replaces Android's timer-only SplashViewModel and iOS's
 * RestoreSessionUseCase call.
 */
class SplashViewModel(private val session: SessionManager) : SharedViewModel() {

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()
    val stateForSwift: CStateFlow<SplashUiState> get() = state.asCStateFlow()

    init {
        scope.launch {
            delay(MIN_DISPLAY_MILLIS)
            _state.value = SplashUiState(
                destination = if (session.isSignedIn) SplashDestination.Main else SplashDestination.Intro,
            )
        }
    }

    companion object {
        const val MIN_DISPLAY_MILLIS = 1_500L
    }
}
