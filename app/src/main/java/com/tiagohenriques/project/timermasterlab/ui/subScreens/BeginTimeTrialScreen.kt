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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.components.SectionHeader
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatDuration

@Composable
fun BeginTimeTrialScreen(navController: NavHostController, screenWidth: Int) {
    val viewModel: BeginTimeTrialViewModel = viewModel(factory = AppViewModelProvider.Factory)
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

            BeginTimeTrialContent(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                onStart = viewModel::startAthlete,
                onStop = viewModel::stopAthlete
            )
        }
    }
}

@Composable
private fun BeginTimeTrialContent(
    modifier: Modifier = Modifier,
    uiState: TimeTrialUiState,
    onStart: (DraftTimeTrialItem) -> Unit,
    onStop: (DraftTimeTrialItem) -> Unit,
) {
    val toStart = uiState.draft.filter { it.result?.startedAt == null }
    val running = uiState.draft.filter {
        it.result?.startedAt != null && (it.result.duration == null || it.result.duration == 0L)
    }
    val finished = uiState.draft.filter {
        it.result?.duration != null && it.result.duration > 0L
    }

    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (toStart.isNotEmpty()) {
            SectionHeader(title = "To start", count = toStart.size)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(toStart, key = { it.athlete.id }) { item ->
                        AthleteRow(item = item, onStart = { onStart(item) }, onStop = { onStop(item) }, true)
                    }
                }
            }

            SectionHeader(title = "Running / Finished", count = running.size + finished.size)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(running, key = { it.athlete.id }) { item ->
                        AthleteRow(item = item, onStart = { onStart(item) }, onStop = { onStop(item) }, true)
                    }
                    items(finished, key = { it.athlete.id }) { item ->
                        AthleteRow(item = item, onStart = { onStart(item) }, onStop = { onStop(item) }, true)
                    }
                }
            }
        } else {
            SectionHeader(title = "Running / Finished", count = running.size + finished.size)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(running + finished, key = { it.athlete.id }) { item ->
                        AthleteRow(item = item, onStart = { onStart(item) }, onStop = { onStop(item) }, true)
                    }
                }
            }
        }
    }
}

@Composable
fun AthleteRow(
    item: DraftTimeTrialItem,
    onStart: () -> Unit,
    onStop: () -> Unit,
    isTimeTrial: Boolean
) {
    val isRunning = item.result?.startedAt != null && item.result.duration == 0L
    val isFinished = item.result?.duration != null && item.result.duration > 0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.startOrder.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.width(12.dp))

            Text(
                text = item.athlete.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = formatDuration(item.result?.duration ?: 0L),
                modifier = Modifier.width(88.dp),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.End
            )

            Spacer(Modifier.width(12.dp))

            when {
                isFinished -> {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                }

                isRunning -> {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .height(48.dp)
                            .width(96.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "STOP",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                else -> {
                    if (isTimeTrial) {
                        Button(
                            onClick = onStart,
                            modifier = Modifier
                                .height(48.dp)
                                .width(96.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "START",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AthleteRowPreview() {
    AthleteRow(
        item = DraftTimeTrialItem(
            sessionId = 0L,
            athlete = Athlete(
                id = 0L,
                name = "John Doe",
                age = 20,
                gender = "M",
                category = 1,
            ),
            result = null,
            startOrder = 1,
            distance = 1000F
        ),
        onStart = {},
        onStop = {},
        isTimeTrial = true
    )
}
