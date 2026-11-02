package com.tiagohenriques.project.timermasterlab.ui.subScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceActionButton
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.responsive.WindowSize
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.TimeTrialRaceBoard
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatLiveDuration

@Composable
fun BeginTimeTrialScreen(navController: NavHostController) {
    val viewModel: BeginTimeTrialViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val sessionTime by viewModel.sessionTime.collectAsState()
    val nowMillis by viewModel.nowMillis.collectAsState()
    var showAbortConfirm by rememberSaveable { mutableStateOf(false) }
    val split = LocalWindowSize.current.widthClass != WindowSize.WidthClass.Compact
    val queued = uiState.draft.filter { it.result?.startedAt == null }
    val running = uiState.draft.filter { item ->
        val result = item.result
        result?.startedAt != null && (result.duration == null || result.duration == 0L)
    }
    val finished = uiState.draft.filter { item ->
        val duration = item.result?.duration
        duration != null && duration > 0L
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(LocalUiMetrics.current.screenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RaceClockHeader(
                sessionTime = sessionTime,
                hasEnded = uiState.hasEnded,
                onAbortOrDone = {
                    if (!uiState.hasEnded) {
                        showAbortConfirm = true
                    } else {
                        navController.navigateUp()
                    }
                },
            )
            TimeTrialRaceBoard(
                queued = queued,
                running = running,
                finished = finished,
                nowMillis = nowMillis,
                split = split,
                onStart = viewModel::startAthlete,
                onStop = viewModel::stopAthlete,
                modifier = Modifier.weight(1f),
            )
        }
    }

    if (showAbortConfirm) {
        AbortSessionDialog(
            onConfirm = {
                showAbortConfirm = false
                viewModel.abortTrial()
                navController.navigateUp()
            },
            onDismiss = { showAbortConfirm = false },
        )
    }
}

@Composable
private fun RaceClockHeader(
    sessionTime: Long,
    hasEnded: Boolean,
    onAbortOrDone: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val metrics = LocalUiMetrics.current
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(metrics.icon),
        )
        Text(
            text = formatLiveDuration(sessionTime),
            style = metrics.clock,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        LiveRaceActionButton(
            label = if (hasEnded) "Done" else "Abort",
            containerColor = if (hasEnded) Color(0xFF2E7D32) else Color(0xFFC62828),
            onClick = onAbortOrDone,
        )
    }
}

@Composable
private fun AbortSessionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Abort session?") },
        text = { Text("This will abort the current session and discard all recorded times.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Abort", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
