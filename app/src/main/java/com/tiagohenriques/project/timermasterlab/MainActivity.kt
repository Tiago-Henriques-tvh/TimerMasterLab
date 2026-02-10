package com.tiagohenriques.project.timermasterlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.domain.UserPreferencesViewModel
import com.tiagohenriques.project.timermasterlab.navigation.AppNavHost
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.TopAppBar
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: UserPreferencesViewModel = viewModel(factory = AppViewModelProvider.Factory)
            val isDarkTheme = viewModel.isDarkThemeFlow.collectAsState(initial = viewModel.getInitialTheme()).value

            TimerMasterLabTheme(
                darkTheme = isDarkTheme,
            ) {
                val layoutDirection = LocalLayoutDirection.current // TODO
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentScreenTitle =
                    TimerMasterLabAppScreens
                        .fromRoute(
                            backStackEntry?.destination?.route,
                        ).title

                Scaffold(
                    modifier =
                        Modifier.padding(
                            start =
                                WindowInsets.safeDrawing
                                    .asPaddingValues()
                                    .calculateLeftPadding(layoutDirection),
                            end =
                                WindowInsets.safeDrawing
                                    .asPaddingValues()
                                    .calculateRightPadding(layoutDirection),
                        ),
                    topBar = {
                        TopAppBar(
                            currentScreenTitle = currentScreenTitle,
                            canNavigateBack =
                                navController.previousBackStackEntry != null && (currentScreenTitle in listOf(
                                    TimerMasterLabAppScreens.Home.title,
                                    TimerMasterLabAppScreens.GroupRace.title,
                                    TimerMasterLabAppScreens.Settings.title,
                                    TimerMasterLabAppScreens.TimeTrial.title,
                                    TimerMasterLabAppScreens.SessionResults.title
                                )),
                            navigateUp = { navController.navigateUp() },
                        )
                    },
                ) { innerPadding ->
                    val windowSize = calculateWindowSizeClass(this)

                    AppNavHost(
                        windowSize = windowSize.widthSizeClass,
                        preferencesViewModel = viewModel,
                        isDarkTheme = isDarkTheme,
                        changeTheme = { viewModel.changeTheme(!isDarkTheme) },
                        navController = navController,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    )
                }
            }
        }
    }
}
