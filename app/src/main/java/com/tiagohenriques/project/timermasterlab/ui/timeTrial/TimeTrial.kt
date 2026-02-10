package com.tiagohenriques.project.timermasterlab.ui.timeTrial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.components.HeaderCard
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

data class TimeTrialScreen(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector,
)

val timeTrialScreensList =
    listOf(
        TimeTrialScreen(
            title = "Start List",
            description = "Add athletes to the start list",
            icon = Icons.Default.Grading,
            route = TimerMasterLabAppScreens.StartList.name,
        ),
        TimeTrialScreen(
            title = "Begin Time Trial",
            description = "Begin the time trial",
            icon = Icons.Default.SkipNext,
            route = TimerMasterLabAppScreens.BeginTimeTrial.name,
        ),
        TimeTrialScreen(
            title = "Sessions Results",
            description = "See the results of the sessions",
            icon = Icons.Default.Analytics,
            route = TimerMasterLabAppScreens.SessionResults.name,
        ),
    )

@Composable
fun TimeTrial(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeaderCard(
            title = "Time Trial",
            description = "Individual timed runs to track performance.\nCreate the start list and start athletes one by one, times are recorded as they finish.",
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.75f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = timeTrialScreensList,
                key = { it.route }
            ) { screen ->
                ScreenCard(
                    title = screen.title,
                    description = screen.description,
                    icon = screen.icon,
                    onClick = { navController.navigate(screen.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(horizontal = 4.dp)
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

@Composable
fun ScreenCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
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
