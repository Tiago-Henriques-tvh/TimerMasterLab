package com.tiagohenriques.project.timermasterlab.ui.groupRace

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Grading
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.components.HeaderCard
import com.tiagohenriques.project.timermasterlab.ui.components.ScreenCard
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

data class GroupRaceScreen(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector,
)

val groupRaceScreensList = listOf(
    GroupRaceScreen(
        title = "Start List",
        description = "Add athletes to the start list",
        icon = Icons.Default.Grading,
        route = "${TimerMasterLabAppScreens.StartList.name}/${Type.fromTypeToString(Type.GROUP_RACE)}",
    ),
    GroupRaceScreen(
        title = "Begin Group Race",
        description = "Begin the side-by-side racing",
        icon = Icons.Default.SkipNext,
        route = TimerMasterLabAppScreens.BeginGroupRace.name,
    ),
    GroupRaceScreen(
        title = "Sessions Results",
        description = "See the results of the sessions",
        icon = Icons.Default.Analytics,
        route = TimerMasterLabAppScreens.SessionResults.name,
    ),
)

@Composable
fun GroupRace(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        HeaderCard(
            title = "Group Race",
            description = "- Side-By-Side timed runs.\n- Create the start list and start athletes in groups, times are recorded as they finish.\n- You can export the start list and the results with filter if you want.",
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.30f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.70f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(
                items = groupRaceScreensList,
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

@Preview(showBackground = true)
@Composable
fun GroupRacePreview() {
    TimerMasterLabTheme {
        GroupRace(navController = rememberNavController())
    }
}
