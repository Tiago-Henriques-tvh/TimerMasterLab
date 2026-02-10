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
    val isBlackTheme: Flow<Boolean> =
        dataStore.data
            .catch {
                if (it is IOException) {
                    Log.e(TAG, "Error reading preferences.", it)
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }.map { preferences ->
                preferences[IS_DARK_THEME] ?: true
            }

    private companion object {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        const val TAG = "UserPreferencesRepo"
    }

    suspend fun saveThemePreference(isDarkTheme: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDarkTheme
        }
    }
}
