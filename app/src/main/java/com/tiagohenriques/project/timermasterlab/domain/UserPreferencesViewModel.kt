package com.tiagohenriques.project.timermasterlab.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiagohenriques.project.timermasterlab.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserPreferencesViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val isDarkThemeFlow: Flow<Boolean> = userPreferencesRepository.isBlackTheme
    val isFileLoggingEnabledFlow: Flow<Boolean> = userPreferencesRepository.isFileLoggingEnabled

    fun changeTheme(isBlackTheme: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(isBlackTheme)
        }
    }

    fun changeFileLogging(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveFileLoggingEnabled(enabled)
        }
    }
}
