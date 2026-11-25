package com.tiagohenriques.project.timermasterlab.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.core.okio.OkioStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.FileSystem
import okio.Path
import okio.buffer

private val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
private val FILE_LOGGING_ENABLED = booleanPreferencesKey("file_logging_enabled")

expect fun getPreferencesFilePath(): Path

private val sharedPreferencesDataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = PreferencesSerializer,
            producePath = ::getPreferencesFilePath,
        ),
    )
}

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences> = sharedPreferencesDataStore,
    private val preferencesPathProvider: () -> Path = ::getPreferencesFilePath,
) {
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_THEME] ?: true
    }

    val isFileLoggingEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FILE_LOGGING_ENABLED] ?: true
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

    suspend fun logDiagnostic(message: String) {
        if (!isFileLoggingEnabled.first()) return
        val directory = checkNotNull(preferencesPathProvider().parent) {
            "The preferences file path has no parent directory."
        }
        FileSystem.SYSTEM.createDirectories(directory)
        val sink = FileSystem.SYSTEM.appendingSink(directory / "timermaster.log").buffer()
        try {
            sink.write("$message\n".encodeToByteArray())
        } finally {
            sink.close()
        }
    }
}
