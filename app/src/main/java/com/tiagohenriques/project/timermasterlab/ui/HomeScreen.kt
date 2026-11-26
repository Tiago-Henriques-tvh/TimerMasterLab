package com.tiagohenriques.project.timermasterlab.ui

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.R
import com.tiagohenriques.project.timermasterlab.navigation.TimerMasterLabAppScreens
import com.tiagohenriques.project.timermasterlab.ui.components.pressScale
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(navController: NavController) {
    val wide = LocalWindowSize.current.widthDp >= 720
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        AnimatedVisibility(
            visible = shown,
            enter = fadeIn(tween(520)) + slideInVertically(tween(520)) { it / 12 },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (wide) {
                WideHome(
                    onTimeTrial = { navController.navigate(TimerMasterLabAppScreens.TimeTrial.name) },
                    onGroupRace = { navController.navigate(TimerMasterLabAppScreens.GroupRace.name) },
                    onSettings = { navController.navigate(TimerMasterLabAppScreens.Settings.name) },
                )
            } else {
                CompactHome(
                    onTimeTrial = { navController.navigate(TimerMasterLabAppScreens.TimeTrial.name) },
                    onGroupRace = { navController.navigate(TimerMasterLabAppScreens.GroupRace.name) },
                    onSettings = { navController.navigate(TimerMasterLabAppScreens.Settings.name) },
                )
            }
        }
    }
}

@Composable
private fun CompactHome(
    onTimeTrial: () -> Unit,
    onGroupRace: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Wordmark()
        Spacer(Modifier.weight(1f))
        TrackClock(align = TextAlign.Start)
        Spacer(Modifier.height(28.dp))
        OpeningLine(align = TextAlign.Start)
        Spacer(Modifier.weight(1.15f))
        RaceActions(onTimeTrial, onGroupRace)
        Spacer(Modifier.height(8.dp))
        SettingsLink(onSettings, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun WideHome(
    onTimeTrial: () -> Unit,
    onGroupRace: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp, vertical = 24.dp),
    ) {
        Wordmark()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            Column(modifier = Modifier.weight(1.15f)) {
                TrackClock(align = TextAlign.Start)
                Spacer(Modifier.height(24.dp))
                OpeningLine(align = TextAlign.Start)
            }
            RaceActions(
                onTimeTrial = onTimeTrial,
                onGroupRace = onGroupRace,
                modifier = Modifier
                    .weight(1f)
                    .widthIn(max = 460.dp),
            )
        }
        SettingsLink(onSettings, modifier = Modifier.align(Alignment.End))
    }
}

@Composable
private fun Wordmark() {
    Text(
        text = stringResource(R.string.app_name).uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelLarge,
        letterSpacing = 1.4.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun TrackClock(align: TextAlign) {
    val context = LocalContext.current
    val use24Hour = remember { DateFormat.is24HourFormat(context) }
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000 - System.currentTimeMillis() % 1_000)
        }
    }
    val locale = Locale.getDefault()
    val time = now.format(DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm:ss" else "h:mm:ss", locale))
    val marker = if (use24Hour) null else now.format(DateTimeFormatter.ofPattern("a", locale))
    val date = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale))

    Column(horizontalAlignment = if (align == TextAlign.Center) Alignment.CenterHorizontally else Alignment.Start) {
        Text(
            text = "Local time",
            style = MaterialTheme.typography.labelMedium,
            letterSpacing = 1.1.sp,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = time,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontFeatureSettings = "tnum",
                    letterSpacing = (-0.8).sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )
            if (marker != null) {
                Text(
                    text = marker,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OpeningLine(align: TextAlign) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = "Hold the start.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Normal,
            textAlign = align,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Trust the finish.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = align,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "A fair clock for every athlete.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = align,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RaceActions(
    onTimeTrial: () -> Unit,
    onGroupRace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeAction(
            title = stringResource(TimerMasterLabAppScreens.TimeTrial.title),
            detail = "Athletes leave one at a time.",
            icon = Icons.Default.Timer,
            accent = MaterialTheme.colorScheme.secondary,
            container = MaterialTheme.colorScheme.secondaryContainer,
            onContainer = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onTimeTrial,
        )
        HomeAction(
            title = stringResource(TimerMasterLabAppScreens.GroupRace.title),
            detail = "A group leaves together.",
            icon = Icons.Default.Groups,
            accent = MaterialTheme.colorScheme.tertiary,
            container = MaterialTheme.colorScheme.tertiaryContainer,
            onContainer = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onGroupRace,
        )
    }
}

@Composable
private fun HomeAction(
    title: String,
    detail: String,
    icon: ImageVector,
    accent: Color,
    container: Color,
    onContainer: Color,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Card(
        onClick = onClick,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(container, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = onContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = accent,
            )
        }
    }
}

@Composable
private fun SettingsLink(onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(text = stringResource(TimerMasterLabAppScreens.Settings.title))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 760)
@Composable
fun HomeScreenPreview() {
    TimerMasterLabTheme {
        HomeScreen(navController = rememberNavController())
    }
}
