package com.cdpg.dhruva.core.network

import kotlinx.serialization.decodeFromString
import com.cdpg.dhruva.core.common.AppClock
import com.cdpg.dhruva.core.common.ProblemType
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.IOException
import kotlinx.serialization.encodeToString

/**
 * An in-process stand-in for the Dhruva backend, speaking the same JSON as the real API.
 * Unlike a fake repository, the real HTTP client, error mapping, token refresh, cache and
 * outbox all run against it — so the whole shared stack is exercised on both platforms.
 *
 * Rules (match Dhruva Android's fakes):
 * - OTP is always 111111.
 * - 9900195577 is a returning user with three addresses; any other number is a new user with none
 *   (so `GET /app/v1/address` answers 404 NoAddressesFound, which the app treats as "empty").
 * - Access tokens stop working after [ACCESS_TOKEN_USES] calls, to exercise the refresh path.
 * - [simulateOffline] makes every call fail like a dropped connection.
 */
class MockDhruvaBackend(private val clock: AppClock) {

    /** Toggled from the My DAs screen in mock builds. */
    val simulateOffline = MutableStateFlow(false)

    private val mutex = Mutex()
    private var counter = 0
    private val accessTokens = mutableMapOf<String, TokenInfo>()
    private val refreshTokens = mutableMapOf<String, String>()
    private val knownUsers = mutableSetOf(RETURNING_USER)
    private val addressesByUser = mutableMapOf<String, MutableList<AddressDto>>()
    private val createdByIdempotencyKey = mutableMapOf<String, AddressDto>()

    init {
        val now = clock.now().toString()
        addressesByUser[RETURNING_USER] = mutableListOf(
            AddressDto("da-1", "home.yeli@dap.dop", "Yeli Home", "home", now, now),
            AddressDto("da-2", "work.navana@dap.dop", "Navana Workspace", "work", now, now),
            AddressDto("da-3", "bnb.bho@dap.dop", "Bhopal BnB", "other", now, now),
        )
    }

    fun engine(): MockEngine = MockEngine { request -> handle(this, request) }

    private suspend fun handle(scope: MockRequestHandleScope, request: HttpRequestData): HttpResponseData {
        delay(LATENCY_MS)
        if (simulateOffline.value) throw IOException("Simulated: no network connection")
        val path = request.url.encodedPath.trimStart('/')
        val body = request.body.toByteArray().decodeToString()
        return mutex.withLock {
            with(scope) {
                when {
                    request.method == HttpMethod.Post && path == ApiPaths.SEND_OTP -> ok(ApiEnvelope<Unit>(ProblemType.SUCCESS, "OTP sent"))
                    request.method == HttpMethod.Post && path == ApiPaths.VERIFY_OTP -> verifyOtp(body)
                    request.method == HttpMethod.Post && path == ApiPaths.REFRESH -> refresh(body)
                    path == ApiPaths.PROFILE -> authorized(request) { ok(ApiEnvelope<Unit>(ProblemType.SUCCESS, "Profile updated")) }
                    request.method == HttpMethod.Get && path == ApiPaths.ADDRESSES -> authorized(request) { phone -> listAddresses(phone) }
                    request.method == HttpMethod.Post && path == ApiPaths.ADDRESSES -> authorized(request) { phone ->
                        createAddress(phone, body, request.headers[ApiHeaders.IDEMPOTENCY_KEY])
                    }
                    else -> problem(HttpStatusCode.NotFound, "urn:dhruva:app:NotFound", "No route $path")
                }
            }
        }
    }

    private fun MockRequestHandleScope.verifyOtp(body: String): HttpResponseData {
        val request = DhruvaJson.decodeFromString<VerifyOtpRequest>(body)
        if (request.otp != VALID_OTP) {
            return problem(HttpStatusCode.BadRequest, ProblemType.INCORRECT_OTP, "Incorrect OTP")
        }
        val isNewUser = knownUsers.add(request.phoneNumber)
        return ok(ApiEnvelope(ProblemType.SUCCESS, "Verified", results = issueTokens(request.phoneNumber, isNewUser)))
    }

    private fun MockRequestHandleScope.refresh(body: String): HttpResponseData {
        val request = DhruvaJson.decodeFromString<RefreshTokenRequest>(body)
        val phone = refreshTokens.remove(request.refreshToken)
            ?: return problem(HttpStatusCode.Unauthorized, ProblemType.AUTHORIZATION_ERROR, "Refresh token rejected")
        return ok(ApiEnvelope(ProblemType.SUCCESS, "Refreshed", results = issueTokens(phone, isNewUser = false)))
    }

    private fun MockRequestHandleScope.listAddresses(phone: String): HttpResponseData {
        val addresses = addressesByUser[phone].orEmpty()
        if (addresses.isEmpty()) {
            return problem(HttpStatusCode.NotFound, ProblemType.NO_ADDRESSES_FOUND, "No addresses found")
        }
        return ok(ApiEnvelope(ProblemType.SUCCESS, "Addresses", results = AddressListResults(addresses)))
    }

    private fun MockRequestHandleScope.createAddress(phone: String, body: String, idempotencyKey: String?): HttpResponseData {
        // Replaying the same Idempotency-Key returns the original result instead of a duplicate.
        idempotencyKey?.let { key ->
            createdByIdempotencyKey[key]?.let { existing ->
                return ok(ApiEnvelope(ProblemType.SUCCESS, "Created", results = AddressResults(existing)), HttpStatusCode.Created)
            }
        }
        val request = DhruvaJson.decodeFromString<CreateAddressRequest>(body)
        val handle = request.prefix + request.suffix
        if (addressesByUser.values.flatten().any { it.digitalAddress.equals(handle, ignoreCase = true) }) {
            return problem(HttpStatusCode.Conflict, ProblemType.HANDLE_TAKEN, "Handle already taken")
        }
        val now = clock.now().toString()
        val created = AddressDto("da-${++counter + 100}", handle, request.title, request.type, now, now)
        addressesByUser.getOrPut(phone) { mutableListOf() }.add(0, created)
        idempotencyKey?.let { createdByIdempotencyKey[it] = created }
        return ok(ApiEnvelope(ProblemType.SUCCESS, "Created", results = AddressResults(created)), HttpStatusCode.Created)
    }

    private fun MockRequestHandleScope.authorized(
        request: HttpRequestData,
        handler: MockRequestHandleScope.(phone: String) -> HttpResponseData,
    ): HttpResponseData {
        val token = request.headers[HttpHeaders.Authorization]?.removePrefix("Bearer ")
        val info = token?.let { accessTokens[it] }
        if (info == null || info.usesLeft <= 0) {
            return problem(HttpStatusCode.Unauthorized, ProblemType.AUTHORIZATION_ERROR, "Access token expired")
        }
        info.usesLeft--
        return handler(info.phone)
    }

    private fun issueTokens(phone: String, isNewUser: Boolean): TokenResults {
        counter++
        val access = "mock-access-$counter"
        val refresh = "mock-refresh-$counter"
        accessTokens[access] = TokenInfo(phone, ACCESS_TOKEN_USES)
        refreshTokens[refresh] = phone
        return TokenResults(access, refresh, isNewUser)
    }

    private inline fun <reified T> MockRequestHandleScope.ok(
        envelope: ApiEnvelope<T>,
        status: HttpStatusCode = HttpStatusCode.OK,
    ): HttpResponseData = respond(DhruvaJson.encodeToString(envelope), status, JSON_HEADERS)

    private fun MockRequestHandleScope.problem(status: HttpStatusCode, type: String, detail: String): HttpResponseData =
        respond(DhruvaJson.encodeToString(ProblemEnvelope(type, status.description, detail)), status, JSON_HEADERS)

    private class TokenInfo(val phone: String, var usesLeft: Int)

    companion object {
        const val VALID_OTP = "111111"
        const val RETURNING_USER = "9900195577"
        private const val LATENCY_MS = 600L
        private const val ACCESS_TOKEN_USES = 5
        private val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
