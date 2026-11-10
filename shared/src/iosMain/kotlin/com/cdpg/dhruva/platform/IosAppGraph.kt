package com.cdpg.dhruva.platform

import com.cdpg.dhruva.core.config.AppConfig
import com.cdpg.dhruva.core.platform.SecureStorage
import com.cdpg.dhruva.di.AppGraph

/**
 * Builds the shared graph for iOS. Called once from Swift:
 * `IosAppGraphKt.createIosAppGraph(secureStorage:deviceLanguageTag:config:)`.
 *
 * [secureStorage] is implemented in Swift (iosApp/Platform/KeychainSecureStorage.swift), reusing
 * Dhruva iOS's Keychain code instead of re-writing it against the C APIs in Kotlin.
 */
fun createIosAppGraph(secureStorage: SecureStorage, deviceLanguageTag: String, config: AppConfig): AppGraph =
    AppGraph(IosPlatformDependencies(secureStorage, deviceLanguageTag), config)

