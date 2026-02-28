package com.tiagohenriques.project.timermasterlab.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

private data class HomeButton(
    val screen: TimerMasterLabAppScreens,
    val icon: ImageVector,
    val startColor: Color,
    val endColor: Color,
)

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun HomeScreen(navController: NavController) {
    val screenWidth = LocalWindowSize.current.widthDp

    val colors = listOf(
        Pair(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary),
        Pair(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary),
        Pair(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.tertiary),
    )

    val buttons = listOf(
        HomeButton(TimerMasterLabAppScreens.TimeTrial, Icons.Default.Timer, colors[0].first, colors[0].second),
        HomeButton(TimerMasterLabAppScreens.GroupRace, Icons.Default.Groups, colors[1].first, colors[1].second),
        HomeButton(TimerMasterLabAppScreens.Settings, Icons.Default.Settings, colors[2].first, colors[2].second),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        val minWidthForTwoColumns = 520.dp
        val columns = if (screenWidth.dp < minWidthForTwoColumns) GridCells.Fixed(1) else GridCells.Fixed(2)

        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(buttons) { button ->
                HomeNavigationButton(
                    icon = button.icon,
                    label = stringResource(id = button.screen.title),
                    onClick = { navController.navigate(button.screen.name) },
                    modifier = Modifier
                        .fillMaxWidth(),
                    startColor = button.startColor,
                    endColor = button.endColor,
                )
            }
        }
    }
}

@Composable
private fun HomeNavigationButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    startColor: Color,
    endColor: Color,
) {
    val screenWidth = LocalWindowSize.current.widthDp
    val horizontalPadding = (screenWidth * 0.08f).dp
    val iconSize = (screenWidth * 0.065f).dp
    val textSize = (screenWidth * 0.050f).sp

    val gradient = Brush.linearGradient(
        colors = listOf(startColor, endColor)
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.25f))
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "$label icon",
                        modifier = Modifier.size(iconSize),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = textSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.8f),
                            offset = Offset(0f, 4f),
                            blurRadius = 12f
                        )
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun HomeScreenPreview() {
    TimerMasterLabTheme {
        HomeScreen(
            navController = rememberNavController(),
        )
    }
}
