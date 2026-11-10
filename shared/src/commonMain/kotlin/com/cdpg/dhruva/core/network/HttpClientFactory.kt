package com.cdpg.dhruva.core.network

import com.cdpg.dhruva.core.config.AppConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** One JSON configuration for the API, the cache payloads and the mock backend. */
val DhruvaJson: Json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    encodeDefaults = true
}

/**
 * The Dhruva API client. Timeouts follow Dhruva Android (connect 15 s / read 30 s).
 * Auth headers are added per call by [DhruvaHttp], not here, so refresh stays in one place.
 */
fun createDhruvaHttpClient(
    engine: HttpClientEngine,
    config: AppConfig,
    languageTag: () -> String,
): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) { json(DhruvaJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }
    defaultRequest {
        url(config.baseUrl)
        headers.append(ApiHeaders.APP_VERSION, config.appVersion)
        headers.append(ApiHeaders.PLATFORM, config.platform)
        headers.append(HttpHeaders.AcceptLanguage, languageTag())
    }
    if (config.enableHttpLogging) {
        install(Logging) {
            // INFO = method, URL and status only. No headers or bodies: no tokens or personal data in logs.
            level = LogLevel.INFO
            logger = Logger.SIMPLE
        }
    }
}

/** For third-party APIs (MapTiler): no Dhruva headers, no auth. */
fun createThirdPartyHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    expectSuccess = false
    install(ContentNegotiation) { json(DhruvaJson) }
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        requestTimeoutMillis = 30_000
    }
}
