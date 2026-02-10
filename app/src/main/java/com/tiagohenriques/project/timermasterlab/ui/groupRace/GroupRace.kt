package com.tiagohenriques.project.timermasterlab.ui.groupRace

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

@Composable
fun GroupRace(navController: NavHostController) {
    Text("hi")
}

@Preview(showBackground = true)
@Composable
fun GroupRacePreview() {
    TimerMasterLabTheme {
        GroupRace(navController = rememberNavController())
    }
}
