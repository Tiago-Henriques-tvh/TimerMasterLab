package com.tiagohenriques.project.timermasterlab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tiagohenriques.project.timermasterlab.ui.HomeScreen
import com.tiagohenriques.project.timermasterlab.ui.groupRace.GroupRace
import com.tiagohenriques.project.timermasterlab.ui.settings.SettingsScreen
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.TimeTrial
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens.BeginTimeTrialScreen
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens.SessionResultsScreen
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens.StartListScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    windowSize: Any,
    preferencesViewModel: Any,
    changeTheme: () -> Unit,
    isDarkTheme: Boolean,
) {
    NavHost(
        navController = navController,
        startDestination = TimerMasterLabAppScreens.Home.name,
        modifier = modifier,
    ) {
        // Home
        composable(TimerMasterLabAppScreens.Home.name) {
            HomeScreen(navController = navController)
        }

        // Group Race
        composable(TimerMasterLabAppScreens.GroupRace.name) {
            GroupRace(navController = navController)
        }

        // Time Trial
        composable(TimerMasterLabAppScreens.TimeTrial.name) {
            TimeTrial(navController = navController)
        }
        // Start List
        composable(TimerMasterLabAppScreens.StartList.name) {
            StartListScreen(navController = navController)
        }
        // Begin Time Trial
        composable(TimerMasterLabAppScreens.BeginTimeTrial.name) {
            BeginTimeTrialScreen(navController = navController)
        }
        // Session Results
        composable(TimerMasterLabAppScreens.SessionResults.name) {
            SessionResultsScreen(navController = navController)
        }

        // Settings
        composable(TimerMasterLabAppScreens.Settings.name) {
            SettingsScreen(
                navController = navController,
                changeTheme = { isBlackTheme -> changeTheme() },
                isBlackTheme = isDarkTheme,
            )
        }
    }
}
