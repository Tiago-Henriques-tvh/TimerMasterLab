package com.tiagohenriques.project.timermasterlab.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.tiagohenriques.project.timermasterlab.ui.HomeScreen
import com.tiagohenriques.project.timermasterlab.ui.groupRace.GroupRace
import com.tiagohenriques.project.timermasterlab.ui.settings.SettingsScreen
import com.tiagohenriques.project.timermasterlab.ui.subScreens.BeginGroupRaceScreen
import com.tiagohenriques.project.timermasterlab.ui.subScreens.BeginTimeTrialScreen
import com.tiagohenriques.project.timermasterlab.ui.subScreens.SessionResultsScreen
import com.tiagohenriques.project.timermasterlab.ui.subScreens.StartListScreen
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.TimeTrial

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
            StartListScreen(navController = navController) // TODO: THIS WILL NEED TO TAKE A PARAMETER FOR TIME TRIAL or GROUP RACE
        }
        // Begin Time Trial
        composable(TimerMasterLabAppScreens.BeginTimeTrial.name) {
            BeginTimeTrialScreen(navController = navController)
        }
        // Begin Group Race
        composable(TimerMasterLabAppScreens.BeginGroupRace.name)
        {
            BeginGroupRaceScreen(navController = navController)
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
