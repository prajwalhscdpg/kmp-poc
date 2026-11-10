package com.cdpg.dhruva.core.platform

import androidx.room.RoomDatabase
import com.cdpg.dhruva.core.database.DhruvaDatabase
import io.ktor.client.engine.HttpClientEngine

/**
 * The few things that genuinely differ per platform. Each app passes an implementation into
 * [com.cdpg.dhruva.di.AppGraph]; everything else is common code.
 *
 * Android: `AndroidPlatformDependencies` (shared/androidMain).
 * iOS: `IosPlatformDependencies` (shared/iosMain), which takes the Keychain storage written in
 * Swift so the proven Keychain code from Dhruva iOS is reused as is.
 */
interface PlatformDependencies {
    val secureStorage: SecureStorage

    /** OkHttp on Android, Darwin (URLSession) on iOS. */
    fun httpEngine(): HttpClientEngine

    fun databaseBuilder(): RoomDatabase.Builder<DhruvaDatabase>

    /** Absolute path of the Preferences DataStore file (must end in `.preferences_pb`). */
    fun preferencesFilePath(): String

    /** Device language, e.g. "hi-IN", used until the user picks one. */
    fun deviceLanguageTag(): String
}

/**
 * Small encrypted key–value store for credentials only (auth tokens).
 * Android: AES-GCM with a key in the Android Keystore. iOS: the Keychain.
 * Synchronous on purpose: values are tiny and both platform APIs are synchronous.
 */
interface SecureStorage {
    fun read(key: String): String?
    fun write(key: String, value: String)
    fun delete(key: String)
}
