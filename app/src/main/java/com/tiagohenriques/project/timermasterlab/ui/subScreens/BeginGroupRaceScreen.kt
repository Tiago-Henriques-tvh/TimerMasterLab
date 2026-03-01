package com.tiagohenriques.project.timermasterlab.ui.subScreens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.components.AthleteResultRow
import com.tiagohenriques.project.timermasterlab.ui.components.FinishedIcon
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatDuration

@Composable
fun BeginGroupRaceScreen(navController: NavHostController) {
    val viewModel: BeginGroupRaceViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val sessionTime by viewModel.sessionTime.collectAsState()
    var showAbortConfirm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer
                Row(
                    modifier = Modifier.weight(0.6f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = formatDuration(sessionTime),
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                }

                // Abort/Save
                FilledIconButton(
                    onClick = {
                        if (!uiState.hasEnded) showAbortConfirm = true
                        else navController.navigateUp()
                    },
                    modifier = Modifier
                        .size(42.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (!uiState.hasEnded)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (uiState.hasEnded) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            if (showAbortConfirm) {
                AlertDialog(
                    onDismissRequest = { showAbortConfirm = false },
                    title = { Text("Abort session?") },
                    text = { Text("This will abort the current session and discard all recorded times.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showAbortConfirm = false
                            viewModel.abortTrial()
                            navController.navigateUp()
                        }) {
                            Text("Abort", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAbortConfirm = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            BeginGroupRaceContent(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onStart = { groupOrder -> viewModel.startAthletes(groupOrder) },
                onStop = { athleteId -> viewModel.stopAthlete(athleteId) }
            )
        }
    }
}

@Composable
private fun BeginGroupRaceContent(
    modifier: Modifier = Modifier,
    uiState: GroupRaceUiState,
    onStart: (Int) -> Unit,
    onStop: (DraftGroupRaceItem) -> Unit,
) {
    val grouped = uiState.draft
        .sortedBy { it.startOrder }
        .groupBy { it.startOrder }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (groupOrder, athletes) ->
            item(key = "group_$groupOrder") {
                val groupStarted = athletes.any { it.result?.startedAt != null }
                val groupFinished = athletes.all { it.result?.duration != null && it.result.duration > 0L }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Group $groupOrder",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            FilterChip(
                                selected = groupFinished,
                                onClick = { /* info-only */ },
                                label = {
                                    Text(
                                        when {
                                            groupFinished -> "Finished"
                                            groupStarted -> "Running"
                                            else -> "Ready"
                                        }
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                )
                            )

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = { onStart(groupOrder) },
                                enabled = !groupStarted,
                                modifier = Modifier.height(44.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    "START",
                                    fontWeight = FontWeight.Bold,
                                    color = if (!groupStarted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        athletes.forEach { item ->
                            AthleteRow(item = item, onStop = { onStop(item) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AthleteRow(
    item: DraftGroupRaceItem,
    onStop: () -> Unit
) {
    val isRunning = item.result?.startedAt != null && item.result.duration == 0L
    val isFinished = item.result?.duration != null && item.result.duration > 0

    AthleteResultRow(
        startOrder = item.startOrder,
        athleteName = item.athlete.name,
        durationText = formatDuration(item.result?.duration ?: 0L),
    ) {
        when {
            isFinished -> {
                FinishedIcon()
            }

            isRunning -> {
                Button(
                    onClick = onStop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F)
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "STOP",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            else -> {
                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    }
}
