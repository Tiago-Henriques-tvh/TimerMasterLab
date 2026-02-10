package com.tiagohenriques.project.timermasterlab

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tiagohenriques.project.timermasterlab.data.AppContainer
import com.tiagohenriques.project.timermasterlab.data.AppDataContainer
import com.tiagohenriques.project.timermasterlab.data.UserPreferencesRepository
import dagger.hilt.android.HiltAndroidApp

private const val LAYOUT_PREFERENCE_NAME = "layout_preferences"
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = LAYOUT_PREFERENCE_NAME,
)

@HiltAndroidApp
public class TimerMasterLabApplication : Application() {
    lateinit var userPreferencesRepository: UserPreferencesRepository
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        userPreferencesRepository = UserPreferencesRepository(dataStore)
        container = AppDataContainer(this)
    }
}
