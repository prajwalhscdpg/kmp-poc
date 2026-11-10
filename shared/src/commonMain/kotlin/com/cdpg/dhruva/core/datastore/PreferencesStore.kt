package com.cdpg.dhruva.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.cdpg.dhruva.core.common.CStateFlow
import com.cdpg.dhruva.core.common.asCStateFlow
import com.cdpg.dhruva.core.strings.AppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import okio.Path.Companion.toPath

fun createPreferencesDataStore(path: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath(produceFile = { path.toPath() })

/**
 * Small, non-sensitive app settings (Preferences DataStore on both platforms).
 * Never business data, never credentials.
 */
class PreferencesStore(
    private val dataStore: DataStore<Preferences>,
    deviceLanguage: AppLanguage,
    scope: CoroutineScope,
) {
    /** The user's language, or the device language until they pick one. */
    val language: StateFlow<AppLanguage> = dataStore.data
        .map { prefs -> prefs[LANGUAGE]?.let { AppLanguage.fromTag(it) } ?: deviceLanguage }
        .stateIn(scope, SharingStarted.Eagerly, deviceLanguage)

    /** For SwiftUI. */
    val languageForSwift: CStateFlow<AppLanguage> get() = language.asCStateFlow()

    val hasSeenIntro: Flow<Boolean> = dataStore.data.map { it[HAS_SEEN_INTRO] ?: false }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { it[LANGUAGE] = language.tag }
    }

    suspend fun setHasSeenIntro() {
        dataStore.edit { it[HAS_SEEN_INTRO] = true }
    }

    private companion object {
        val LANGUAGE = stringPreferencesKey("app_language")
        val HAS_SEEN_INTRO = booleanPreferencesKey("has_seen_intro")
    }
}
