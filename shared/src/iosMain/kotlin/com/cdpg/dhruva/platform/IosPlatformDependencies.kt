package com.cdpg.dhruva.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import com.cdpg.dhruva.core.database.DhruvaDatabase
import com.cdpg.dhruva.core.platform.PlatformDependencies
import com.cdpg.dhruva.core.platform.SecureStorage
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

class IosPlatformDependencies(
    override val secureStorage: SecureStorage,
    private val languageTag: String,
) : PlatformDependencies {

    override fun httpEngine(): HttpClientEngine = Darwin.create()

    override fun databaseBuilder(): RoomDatabase.Builder<DhruvaDatabase> =
        Room.databaseBuilder<DhruvaDatabase>(name = "${appSupportDirectory()}/dhruva.db")

    override fun preferencesFilePath(): String = "${appSupportDirectory()}/dhruva.preferences_pb"

    override fun deviceLanguageTag(): String = languageTag

    /** Application Support: not user-visible, backed up, not purged like Caches. */
    @OptIn(ExperimentalForeignApi::class)
    private fun appSupportDirectory(): String {
        val url: NSURL? = NSFileManager.defaultManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )
        return requireNotNull(url?.path) { "Application Support directory unavailable" }
    }
}
