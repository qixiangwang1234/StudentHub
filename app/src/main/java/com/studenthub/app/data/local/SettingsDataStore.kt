package com.studenthub.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "studenthub_settings")

data class AppSettings(
    val apiKey: String = "",
    val apiModel: String = "deepseek-chat",
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val API_KEY = stringPreferencesKey("deepseek_api_key")
        val API_MODEL = stringPreferencesKey("api_model")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val settings: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            apiKey = prefs[Keys.API_KEY] ?: "",
            apiModel = prefs[Keys.API_MODEL] ?: "deepseek-chat",
            darkMode = prefs[Keys.DARK_MODE] ?: false,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_KEY] = apiKey
        }
    }

    suspend fun saveApiModel(model: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.API_MODEL] = model
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}
