package com.tiagohenriques.project.timermasterlab

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tiagohenriques.project.timermasterlab.data.AppContainer
import com.tiagohenriques.project.timermasterlab.data.AppDataContainer
import com.tiagohenriques.project.timermasterlab.data.UserPreferencesRepository
import com.tiagohenriques.project.timermasterlab.logging.AppLogger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val LAYOUT_PREFERENCE_NAME = "layout_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LAYOUT_PREFERENCE_NAME,
)

@HiltAndroidApp
public class TimerMasterLabApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var container: AppContainer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        AppLogger.init(this)
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        container = AppDataContainer(this)
        applicationScope.launch {
            val enabled = userPreferencesRepository.isFileLoggingEnabled.first()
            AppLogger.setEnabled(this@TimerMasterLabApplication, enabled)
        }
    }
}
