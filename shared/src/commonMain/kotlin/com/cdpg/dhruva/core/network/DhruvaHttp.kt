package com.cdpg.dhruva.core.network

import com.cdpg.dhruva.core.common.DanAiaError
import com.cdpg.dhruva.core.common.DanAiaResult
import com.cdpg.dhruva.core.session.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.ContentConvertException
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/** Thrown inside [apiCall] for a non-2xx response; turned into [DanAiaResult.Error]. */
class ApiException(val error: DanAiaError) : Exception(error.toString())

/**
 * Sends Dhruva API requests. For authenticated calls it adds the bearer token and, on a 401,
 * refreshes once (through [TokenRefresher], one refresh at a time) and retries the call.
 */
class DhruvaHttp(
    val client: HttpClient,
    private val session: SessionManager,
    private val refresher: TokenRefresher,
) {
    suspend fun execute(
        authenticated: Boolean,
        block: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val token = if (authenticated) session.accessToken() else null
        val response = client.request {
            block()
            if (token != null) bearerAuth(token)
        }
        if (token == null || response.status != HttpStatusCode.Unauthorized) return response

        val freshToken = refresher.refresh(staleAccessToken = token) ?: return response
        return client.request {
            block()
            bearerAuth(freshToken)
        }
    }
}

/**
 * Sets a JSON body. Content-Type is set here, per request, not in defaultRequest: Ktor rejects
 * a Content-Type header on requests without a body.
 */
inline fun <reified T> HttpRequestBuilder.jsonBody(body: T) {
    contentType(ContentType.Application.Json)
    setBody(body)
}

/** Maps a non-2xx response to the shared error type, using the envelope's problem `type`. */
suspend fun HttpResponse.toDanAiaError(): DanAiaError {
    val type = try {
        body<ProblemEnvelope>().type
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        null
    }
    return when (status.value) {
        401, 403 -> DanAiaError.Unauthorized
        404 -> DanAiaError.NotFound(type)
        400, 409, 422 -> DanAiaError.Validation(type)
        else -> DanAiaError.Server(status.value)
    }
}

/** Decodes a 2xx body, or throws [ApiException] with the mapped error. */
suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
    if (!status.isSuccess()) throw ApiException(toDanAiaError())
    return body()
}

suspend fun HttpResponse.requireSuccess() {
    if (!status.isSuccess()) throw ApiException(toDanAiaError())
}

/**
 * Runs a network call and turns every failure into a typed [DanAiaError].
 * Cancellation is always rethrown (Dhruva engineering rule).
 */
suspend inline fun <T> apiCall(block: () -> T): DanAiaResult<T> = try {
    DanAiaResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: ApiException) {
    DanAiaResult.Error(e.error)
} catch (e: HttpRequestTimeoutException) {
    DanAiaResult.Error(DanAiaError.Timeout)
} catch (e: ConnectTimeoutException) {
    DanAiaResult.Error(DanAiaError.Timeout)
} catch (e: SocketTimeoutException) {
    DanAiaResult.Error(DanAiaError.Timeout)
} catch (e: SerializationException) {
    DanAiaResult.Error(DanAiaError.Unexpected)
} catch (e: ContentConvertException) {
    DanAiaResult.Error(DanAiaError.Unexpected)
} catch (e: Throwable) {
    // Engine errors differ per platform (IOException on OkHttp, DarwinHttpRequestException on
    // iOS); all of them mean the request didn't get through.
    DanAiaResult.Error(DanAiaError.Network)
}
