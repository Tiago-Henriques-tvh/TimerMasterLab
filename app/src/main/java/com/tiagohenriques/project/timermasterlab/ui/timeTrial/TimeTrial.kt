package com.tiagohenriques.project.timermasterlab.ui.timeTrial

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.R
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.components.HeaderCard
import com.tiagohenriques.project.timermasterlab.ui.components.ScreenCard
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.responsive.WindowSize
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

data class TimeTrialScreen(
    val title: String,
    val route: String,
    val icon: ImageVector,
)

val timeTrialScreensList = listOf(
    TimeTrialScreen(
        title = "Start List",
        icon = Icons.AutoMirrored.Filled.Grading,
        route = "${TimerMasterLabAppScreens.StartList.name}/${Type.fromTypeToString(Type.TIME_TRIAL)}",
    ),
    TimeTrialScreen(
        title = "Begin Time Trial",
        icon = Icons.Default.SkipNext,
        route = TimerMasterLabAppScreens.BeginTimeTrial.name,
    ),
    TimeTrialScreen(
        title = "Sessions Results",
        icon = Icons.Default.Analytics,
        route = TimerMasterLabAppScreens.SessionResults.name,
    ),
)

@Composable
fun TimeTrial(navController: NavHostController) {
    val windowSize = LocalWindowSize.current

    val horizontalPadding = when (windowSize.widthClass) {
        WindowSize.WidthClass.Compact -> 16.dp
        WindowSize.WidthClass.Medium -> 24.dp
        WindowSize.WidthClass.Expanded -> 32.dp
    }

    val itemHeight = LocalUiMetrics.current.menuRowHeight

    val visible = remember { mutableStateListOf<Boolean>() }
    LaunchedEffect(Unit) {
        visible.clear()
        repeat(timeTrialScreensList.size) { visible.add(false) }
        timeTrialScreensList.indices.forEach { i ->
            kotlinx.coroutines.delay(60L * i)
            visible[i] = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding)
            .padding(vertical = 16.dp)
    ) {
        HeaderCard(
            title = "Time Trial",
            painter = painterResource(R.drawable.time_trial_img),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Actions",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 6.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 12.dp, top = 4.dp)
        ) {
            items(
                items = timeTrialScreensList,
                key = { it.route }
            ) { screen ->
                val index = timeTrialScreensList.indexOfFirst { it.route == screen.route }
                val show = index >= 0 && index < visible.size && visible[index]

                AnimatedVisibility(
                    visible = show,
                    enter = fadeIn(animationSpec = tween(220)) +
                        slideInVertically(animationSpec = tween(220)) { it / 4 },
                ) {
                    ScreenCard(
                        title = screen.title,
                        icon = screen.icon,
                        onClick = { navController.navigate(screen.route) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(itemHeight)
                            .padding(horizontal = 2.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(20.dp),
                                clip = false
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimeTrialPreview() {
    TimerMasterLabTheme {
        TimeTrial(
            navController = NavHostController(LocalContext.current),
        )
    }
}
