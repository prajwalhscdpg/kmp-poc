package com.cdpg.dhruva.core.network

import com.cdpg.dhruva.core.session.AuthTokens
import com.cdpg.dhruva.core.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.isSuccess
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/**
 * Refreshes the access token. Parallel 401s trigger exactly one refresh: callers queue on the
 * mutex, and a caller whose token was already replaced just takes the new one.
 *
 * A refused refresh ends the session ([SessionManager.expire] → both apps go to Login).
 * A network failure does not: the user stays signed in and the call fails as Network.
 */
class TokenRefresher(
    private val client: HttpClient,
    private val session: SessionManager,
) {
    private val mutex = Mutex()

    /** Returns a usable access token, or null if there is none. */
    suspend fun refresh(staleAccessToken: String): String? = mutex.withLock {
        val current = session.currentTokens() ?: return null
        if (current.accessToken != staleAccessToken) return current.accessToken

        val response = try {
            client.post(ApiPaths.REFRESH) { jsonBody(RefreshTokenRequest(current.refreshToken)) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            return null
        }

        val results = if (response.status.isSuccess()) {
            try {
                response.body<ApiEnvelope<TokenResults>>().results
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                null
            }
        } else {
            null
        }
        if (results == null) {
            session.expire()
            return null
        }
        val tokens = AuthTokens(results.accessToken, results.refreshToken)
        session.updateTokens(tokens)
        tokens.accessToken
    }
}
