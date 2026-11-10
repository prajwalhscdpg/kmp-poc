package com.cdpg.dhruva.data

import com.cdpg.dhruva.core.common.AppClock
import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.config.AppConfigs
import com.cdpg.dhruva.core.network.DhruvaHttp
import com.cdpg.dhruva.core.network.DhruvaJson
import com.cdpg.dhruva.core.network.MockDhruvaBackend
import com.cdpg.dhruva.core.network.TokenRefresher
import com.cdpg.dhruva.core.network.createDhruvaHttpClient
import com.cdpg.dhruva.core.platform.SecureStorage
import com.cdpg.dhruva.core.session.SessionManager
import com.cdpg.dhruva.core.session.SessionState
import com.cdpg.dhruva.core.session.TokenStore
import com.cdpg.dhruva.data.auth.AuthRepositoryImpl
import com.cdpg.dhruva.data.profile.ProfileRepositoryImpl
import com.cdpg.dhruva.domain.auth.OtpVerification
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

/**
 * The real HTTP client, envelopes, error mapping, token storage and refresh, against the mock
 * backend. Runs on JVM and iOS, so it also proves the Ktor setup on both.
 */
class NetworkStackTest {

    private class MemoryStorage : SecureStorage {
        val values = mutableMapOf<String, String>()
        override fun read(key: String) = values[key]
        override fun write(key: String, value: String) { values[key] = value }
        override fun delete(key: String) { values.remove(key) }
    }

    private val backend = MockDhruvaBackend(AppClock.System)
    private val session = SessionManager(TokenStore(MemoryStorage(), DhruvaJson)) {}
    private val client = createDhruvaHttpClient(backend.engine(), AppConfigs.mock("test", "test").copy(enableHttpLogging = false)) { "en" }
    private val http = DhruvaHttp(client, session, TokenRefresher(client, session))
    private val auth = AuthRepositoryImpl(http, session)
    private val profile = ProfileRepositoryImpl(http)

    @Test fun incorrectOtpIsAResultNotAnError() = runTest {
        val result = auth.verifyOtp("9900195577", "000000")
        assertEquals(DanAiaResult.Success(OtpVerification.Incorrect), result)
        assertEquals(SessionState.SignedOut, session.state.value)
    }

    @Test fun verifiedOtpStartsTheSession() = runTest {
        val result = auth.verifyOtp("9876543210", MockDhruvaBackend.VALID_OTP)
        assertEquals(DanAiaResult.Success(OtpVerification.Verified(isNewUser = true)), result)
        assertEquals(SessionState.Authenticated, session.state.value)
        assertNotNull(session.accessToken())
    }

    @Test fun expiredAccessTokenIsRefreshedTransparently() = runTest {
        auth.verifyOtp("9900195577", MockDhruvaBackend.VALID_OTP)
        val firstToken = session.accessToken()
        // The mock accepts each access token 5 times; the 6th call has to refresh.
        repeat(8) { assertIs<DanAiaResult.Success<Unit>>(profile.updateFullName("Ravi Kumar")) }
        assertNotEquals(firstToken, session.accessToken())
        assertEquals(SessionState.Authenticated, session.state.value)
    }

    @Test fun refusedRefreshEndsTheSession() = runTest {
        auth.verifyOtp("9900195577", MockDhruvaBackend.VALID_OTP)
        // Corrupt the refresh token so the server refuses it.
        session.updateTokens(session.currentTokens()!!.copy(refreshToken = "revoked"))
        repeat(5) { profile.updateFullName("Ravi Kumar") }
        val result = profile.updateFullName("Ravi Kumar")
        assertEquals(DanAiaResult.Error(DanAiaError.Unauthorized), result)
        assertEquals(SessionState.SignedOut, session.state.value)
    }

    @Test fun noConnectionIsANetworkError() = runTest {
        backend.simulateOffline.value = true
        assertEquals(DanAiaResult.Error(DanAiaError.Network), auth.requestOtp("9900195577"))
    }
}
