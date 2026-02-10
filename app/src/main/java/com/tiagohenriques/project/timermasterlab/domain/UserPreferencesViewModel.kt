package com.tiagohenriques.project.timermasterlab.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiagohenriques.project.timermasterlab.data.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class UserPreferencesViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    val isDarkThemeFlow: Flow<Boolean> = userPreferencesRepository.isBlackTheme

    fun getInitialTheme(): Boolean =
        runBlocking {
            userPreferencesRepository.isBlackTheme.firstOrNull() ?: false
        }

    fun changeTheme(isBlackTheme: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.saveThemePreference(isBlackTheme)
        }
    }
}
