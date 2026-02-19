package com.tiagohenriques.project.timermasterlab.ui.subScreens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatDuration

@Composable
fun BeginGroupRaceScreen(navController: NavHostController, screenWidth: Int) {
    val viewModel: BeginGroupRaceViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val sessionTime by viewModel.sessionTime.collectAsState()
    var showAbortConfirm by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = formatDuration(sessionTime),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                FilledIconButton(
                    onClick = {
                        if (!uiState.hasEnded) showAbortConfirm = true
                        else navController.navigateUp()
                    },
                    modifier = Modifier.size(56.dp),
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
                        tint = Color.White
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        grouped.forEach { (groupOrder, athletes) ->
            item(key = "group_$groupOrder") {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Group $groupOrder",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.weight(1f)
                            )

                            val groupStarted =
                                athletes.any { it.result?.startedAt != null }

                            FilledTonalButton(
                                onClick = { onStart(groupOrder) },
                                enabled = !groupStarted
                            ) {
                                Text(
                                    if (groupStarted) "Started"
                                    else "Start"
                                )
                            }
                        }

                        athletes.forEach { item ->
                            val duration = item.result?.duration ?: 0L
                            val hasStarted = item.result?.startedAt != null
                            val hasFinished = duration > 0L
                            // USE AthleteRow
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Column(modifier = Modifier.weight(1f)) {

                                        Text(
                                            text = item.athlete.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )

                                        Spacer(Modifier.height(4.dp))

                                        Text(
                                            text = when {
                                                !hasStarted -> "Not started"
                                                hasFinished -> formatDuration(duration)
                                                else -> "Running..."
                                            },
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }

                                    if (hasStarted && !hasFinished) {
                                        FilledTonalButton(
                                            onClick = { onStop(item) }
                                        ) {
                                            Text("Stop")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

