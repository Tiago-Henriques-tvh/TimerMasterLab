package com.tiagohenriques.project.timermasterlab.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) {
    private val preferences: Flow<Preferences> =
        dataStore.data.catch {
            if (it is IOException) {
                Log.e(TAG, "Error reading preferences.", it)
                emit(emptyPreferences())
            } else {
                throw it
            }
        }

    val isBlackTheme: Flow<Boolean> =
        preferences.map { stored ->
            stored[IS_DARK_THEME] ?: true
        }

    val isFileLoggingEnabled: Flow<Boolean> =
        preferences.map { stored ->
            stored[FILE_LOGGING_ENABLED] ?: true
        }

    private companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val FILE_LOGGING_ENABLED = booleanPreferencesKey("file_logging_enabled")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun saveThemePreference(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
        }
    }

    suspend fun saveFileLoggingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FILE_LOGGING_ENABLED] = enabled
        }
    }
}
