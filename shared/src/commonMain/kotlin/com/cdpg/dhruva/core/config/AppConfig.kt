package com.cdpg.dhruva.core.config

/**
 * Per-build settings, created by each app at startup (Android: BuildConfig, iOS: Info.plist /
 * Secrets.swift). Nothing secret is committed.
 *
 * @param baseUrl Dhruva API base URL, with trailing slash.
 * @param useMockBackend true serves every Dhruva API call from the in-process mock backend
 *   (OTP is 111111), so the whole stack runs without a server. There is no real backend yet.
 * @param mapTilerApiKey key for MapTiler geocoding (Create DA location search); blank disables it.
 * @param appVersion sent as `X-App-Version` so the server can enforce a minimum version.
 * @param platform "android" or "ios", sent as `X-Platform`.
 * @param enableHttpLogging logs request lines (never bodies or headers: no personal data in logs).
 */
data class AppConfig(
    val baseUrl: String,
    val useMockBackend: Boolean,
    val mapTilerApiKey: String,
    val appVersion: String,
    val platform: String,
    val enableHttpLogging: Boolean,
)

/** Builders callable from Swift (Kotlin default arguments don't reach Swift). */
object AppConfigs {
    const val PLACEHOLDER_BASE_URL = "https://api.dev.dhruva.gov.in/"

    /** Mock backend, no MapTiler key: what the POC runs with out of the box. */
    fun mock(appVersion: String, platform: String): AppConfig = AppConfig(
        baseUrl = PLACEHOLDER_BASE_URL,
        useMockBackend = true,
        mapTilerApiKey = "",
        appVersion = appVersion,
        platform = platform,
        enableHttpLogging = true,
    )
}
