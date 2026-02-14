package com.tiagohenriques.project.timermasterlab.di

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.tiagohenriques.project.timermasterlab.TimerMasterLabApplication
import com.tiagohenriques.project.timermasterlab.domain.UserPreferencesViewModel
import com.tiagohenriques.project.timermasterlab.ui.subScreens.BeginTimeTrialViewModel
import com.tiagohenriques.project.timermasterlab.ui.subScreens.SessionResultViewModel
import com.tiagohenriques.project.timermasterlab.ui.subScreens.StartListViewModel

object AppViewModelProvider {
    val Factory =
        viewModelFactory {
            initializer {
                StartListViewModel(
                    timerMasterLabApplication().container.athletesRepository,
                    timerMasterLabApplication().container.startListEntryRepository,
                )
            }

            initializer {
                SessionResultViewModel(
                    timerMasterLabApplication().container.sessionResultRepository,
                    timerMasterLabApplication().container.sessionRepository,
                    timerMasterLabApplication().container.athletesRepository,
                )
            }

            initializer {
                BeginTimeTrialViewModel(
                    timerMasterLabApplication().container.sessionResultRepository,
                    timerMasterLabApplication().container.sessionRepository,
                    timerMasterLabApplication().container.athletesRepository,
                    timerMasterLabApplication().container.startListEntryRepository,
                )
            }

            initializer {
                val application = (this[APPLICATION_KEY] as TimerMasterLabApplication)
                UserPreferencesViewModel(application.userPreferencesRepository)
            }
        }
}

fun CreationExtras.timerMasterLabApplication(): TimerMasterLabApplication =
    (this[APPLICATION_KEY] as TimerMasterLabApplication)
