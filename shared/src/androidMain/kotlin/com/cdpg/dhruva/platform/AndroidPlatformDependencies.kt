package com.cdpg.dhruva.platform

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cdpg.dhruva.core.config.AppConfig
import com.cdpg.dhruva.core.database.DhruvaDatabase
import com.cdpg.dhruva.core.platform.PlatformDependencies
import com.cdpg.dhruva.core.platform.SecureStorage
import com.cdpg.dhruva.di.AppGraph
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import java.security.KeyStore
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Builds the shared graph for Android. Call once, from the Application. */
fun createAndroidAppGraph(context: Context, config: AppConfig): AppGraph =
    AppGraph(AndroidPlatformDependencies(context.applicationContext), config)

class AndroidPlatformDependencies(private val context: Context) : PlatformDependencies {

    override val secureStorage: SecureStorage by lazy { KeystoreSecureStorage(context) }

    override fun httpEngine(): HttpClientEngine = OkHttp.create()

    override fun databaseBuilder(): RoomDatabase.Builder<DhruvaDatabase> {
        val file = context.getDatabasePath(DATABASE_NAME)
        return Room.databaseBuilder<DhruvaDatabase>(context = context, name = file.absolutePath)
    }

    override fun preferencesFilePath(): String =
        context.filesDir.resolve("dhruva.preferences_pb").absolutePath

    override fun deviceLanguageTag(): String = Locale.getDefault().toLanguageTag()

    private companion object {
        const val DATABASE_NAME = "dhruva.db"
    }
}

/**
 * AES-256-GCM with a non-exportable key in the Android Keystore; ciphertext (IV + data, Base64)
 * in private SharedPreferences. Dhruva Android planned Tink for this; plain Keystore keeps the
 * POC dependency-free and is equally strong for a handful of small values.
 */
internal class KeystoreSecureStorage(context: Context) : SecureStorage {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun read(key: String): String? {
        val stored = prefs.getString(key, null) ?: return null
        return try {
            val bytes = Base64.decode(stored, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, IV_LENGTH)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_LENGTH_BITS, iv))
            String(cipher.doFinal(bytes, IV_LENGTH, bytes.size - IV_LENGTH), Charsets.UTF_8)
        } catch (e: Exception) {
            // Key lost (e.g. restored backup on a new device): treat as signed out.
            prefs.edit().remove(key).apply()
            null
        }
    }

    @Synchronized
    override fun write(key: String, value: String) {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val encrypted = cipher.iv + cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        prefs.edit().putString(key, Base64.encodeToString(encrypted, Base64.NO_WRAP)).apply()
    }

    @Synchronized
    override fun delete(key: String) {
        prefs.edit().remove(key).apply()
    }

    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val PREFS_NAME = "dhruva_secure"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "dhruva_token_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_LENGTH = 12
        const val TAG_LENGTH_BITS = 128
    }
}
