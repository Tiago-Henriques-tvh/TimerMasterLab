package com.tiagohenriques.project.timermasterlab.data.preferences

import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserPreferencesRepositoryTest {
    @Test
    fun preferencesPersistAndFileLoggingRespectsItsSetting() = runBlocking {
        val directory = File.createTempFile("timer-master-preferences", "").apply {
            delete()
            mkdirs()
        }
        val preferencesPath = File(directory, "preferences.preferences_pb").absolutePath.toPath()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            storage = OkioStorage(
                fileSystem = FileSystem.SYSTEM,
                serializer = PreferencesSerializer,
                producePath = { preferencesPath },
            ),
            scope = scope,
        )

        try {
            val repository = UserPreferencesRepository(
                dataStore = dataStore,
                preferencesPathProvider = { preferencesPath },
            )

            assertTrue(repository.isDarkTheme.first())
            assertTrue(repository.isFileLoggingEnabled.first())

            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey("is_dark_theme")] = false
                preferences[booleanPreferencesKey("file_logging_enabled")] = false
            }
            repository.logDiagnostic("not written")
            assertFalse(repository.isDarkTheme.first())
            assertFalse(repository.isFileLoggingEnabled.first())
            assertFalse(File(directory, "timermaster.log").exists())

            repository.saveThemePreference(true)
            repository.saveFileLoggingEnabled(true)
            assertTrue(repository.isDarkTheme.first())
            repository.logDiagnostic("diagnostic event")
            assertEquals("diagnostic event\n", File(directory, "timermaster.log").readText())
        } finally {
            scope.cancel()
            directory.deleteRecursively()
        }
    }
}
