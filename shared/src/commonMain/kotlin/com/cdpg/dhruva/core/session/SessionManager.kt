package com.cdpg.dhruva.core.session

import com.cdpg.dhruva.core.common.CFlow
import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.asCFlow
import com.cdpg.dhruva.core.common.asCStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

enum class SessionState { SignedOut, Authenticated }

/**
 * Who is signed in. Both apps route on [state]: SignedOut → Login, Authenticated → Main.
 *
 * Logout always completes: tokens and every cached record are wiped even if the server call
 * fails (Dhruva architecture §5).
 */
class SessionManager(
    private val tokenStore: TokenStore,
    private val wipeLocalData: suspend () -> Unit,
) {
    private val _state = MutableStateFlow(
        if (tokenStore.load() != null) SessionState.Authenticated else SessionState.SignedOut,
    )
    val state: StateFlow<SessionState> = _state.asStateFlow()

    /** Emits once each time the server rejects the session (refresh token refused). */
    private val _expired = Channel<Unit>(Channel.CONFLATED)
    val sessionExpired: Flow<Unit> = _expired.receiveAsFlow()

    /** For SwiftUI. */
    val stateForSwift: CStateFlow<SessionState> get() = state.asCStateFlow()
    val sessionExpiredForSwift: CFlow<Unit> get() = sessionExpired.asCFlow()

    val isSignedIn: Boolean get() = _state.value == SessionState.Authenticated

    fun accessToken(): String? = tokenStore.load()?.accessToken

    fun startSession(tokens: AuthTokens) {
        tokenStore.save(tokens)
        _state.value = SessionState.Authenticated
    }

    fun updateTokens(tokens: AuthTokens) {
        tokenStore.save(tokens)
    }

    fun currentTokens(): AuthTokens? = tokenStore.load()

    suspend fun signOut() {
        tokenStore.clear()
        wipeLocalData()
        _state.value = SessionState.SignedOut
    }

    /** Called by the network layer when a refresh is refused. */
    suspend fun expire() {
        if (_state.value == SessionState.SignedOut) return
        signOut()
        _expired.trySend(Unit)
    }
}
