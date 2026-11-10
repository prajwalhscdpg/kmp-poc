package com.cdpg.dhruva.core.session

import com.cdpg.dhruva.core.platform.SecureStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

/** Auth tokens in [SecureStorage]. The only credentials the app keeps. */
class TokenStore(private val storage: SecureStorage, private val json: Json) {

    fun load(): AuthTokens? = storage.read(KEY)?.let { raw ->
        runCatching { json.decodeFromString(AuthTokens.serializer(), raw) }.getOrNull()
    }

    fun save(tokens: AuthTokens) {
        storage.write(KEY, json.encodeToString(AuthTokens.serializer(), tokens))
    }

    fun clear() {
        storage.delete(KEY)
    }

    private companion object {
        // Same key as Dhruva iOS's DefaultAuthTokenRepository.
        const val KEY = "com.dap.authTokens"
    }
}
