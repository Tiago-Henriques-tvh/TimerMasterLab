package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils
import com.tiagohenriques.project.timermasterlab.ui.utils.shareStartListFile
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SessionResultsScreen(navController: NavHostController) {
    val viewModel: SessionResultViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var sessionMenuExpanded by remember { mutableStateOf(false) }
    var groupMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SessionResultViewModel.UiEvent.Message ->
                    Toast.makeText(context, event.text, Toast.LENGTH_LONG).show()
                is SessionResultViewModel.UiEvent.ShareFile ->
                    shareStartListFile(context, event.file)
            }
        }
    }

    LaunchedEffect(uiState.sessions, uiState.selectedSession) {
        if (uiState.selectedSession == null) {
            uiState.sessions.maxByOrNull { it.sessionId }?.let { latest ->
                viewModel.setSelectedSession(latest.sessionId)
            }
        }
    }

    val selectedSession = uiState.selectedSession
    val sessionDate = selectedSession?.let { Utils.formatSessionDate(it.date) }.orEmpty()
    val allRows = uiState.sections.flatMap { it.items }
    val finishedRows = allRows
        .filter { (it.result.duration ?: 0L) > 0L }
        .sortedBy { it.result.duration }
    val averageTime = finishedRows
        .mapNotNull { it.result.duration }
        .takeIf { it.isNotEmpty() }
        ?.average()
        ?.toLong()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "SESSION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(
                        onClick = { sessionMenuExpanded = true },
                        enabled = uiState.sessions.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = when {
                                    selectedSession == null && uiState.sessions.isEmpty() -> "No sessions yet"
                                    selectedSession == null -> "Choose a session"
                                    sessionDate.isNotBlank() -> sessionDate
                                    else -> "Session #${selectedSession.sessionId}"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (selectedSession != null && sessionDate.isNotBlank()) {
                                Text(
                                    text = "Session #${selectedSession.sessionId}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                        Icon(
                            Icons.Filled.FilterList,
                            contentDescription = "Choose session",
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    DropdownMenu(
                        expanded = sessionMenuExpanded,
                        onDismissRequest = { sessionMenuExpanded = false },
                    ) {
                        uiState.sessions
                            .sortedByDescending { it.sessionId }
                            .forEach { session ->
                                val date = Utils.formatSessionDate(session.date)
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = date.ifBlank { "Session #${session.sessionId}" },
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                            if (date.isNotBlank()) {
                                                Text(
                                                    text = "Session #${session.sessionId}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        viewModel.setSelectedSession(session.sessionId)
                                        sessionMenuExpanded = false
                                    },
                                )
                            }
                    }
                }
            }
        }

        if (selectedSession == null) {
            EmptyResultsCard(
                modifier = Modifier.weight(1f),
                title = if (uiState.sessions.isEmpty()) "No sessions yet" else "Loading session",
                message = if (uiState.sessions.isEmpty()) {
                    "Complete a Time Trial or Group Race to see its results here."
                } else {
                    "Preparing the latest session results."
                },
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { groupMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            Icons.Filled.FilterAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (uiState.groupBy == "None") "All results" else "Group: ${uiState.groupBy}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    DropdownMenu(
                        expanded = groupMenuExpanded,
                        onDismissRequest = { groupMenuExpanded = false },
                    ) {
                        listOf("None", "Age", "Gender", "Category", "Distance").forEach { group ->
                            DropdownMenuItem(
                                text = { Text(if (group == "None") "No grouping" else group) },
                                onClick = {
                                    viewModel.setGroupBy(group)
                                    groupMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = { viewModel.exportCurrentResultsToPdf(context) },
                    enabled = !uiState.isExporting && uiState.totalCount > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        Icons.Filled.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (uiState.isExporting) "Exporting..." else "Export")
                }
            }

            if (uiState.totalCount == 0) {
                EmptyResultsCard(
                    modifier = Modifier.weight(1f),
                    title = "No results in this session",
                    message = "Results will appear here as athletes complete their runs.",
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ResultMetric(
                        modifier = Modifier.weight(1f),
                        label = "FINISHED",
                        value = "${finishedRows.size} / ${uiState.totalCount}",
                    )
                    ResultMetric(
                        modifier = Modifier.weight(1f),
                        label = "AVERAGE TIME",
                        value = averageTime?.let(Utils::formatDuration) ?: "N/A",
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Leaderboard",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = if (uiState.groupBy == "None") "Fastest first" else "Rank within group",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    item { ResultTableHeader() }
                    if (uiState.groupBy == "None") {
                        val rows = uiState.sections.firstOrNull()?.items.orEmpty()
                        itemsIndexed(
                            rows,
                            key = { _, row -> "${row.result.sessionId}-${row.result.athleteId}" },
                        ) { index, row ->
                            val rank = if ((row.result.duration ?: 0L) > 0L) index + 1 else null
                            ResultTableRow(rank, row.result, row.athlete)
                        }
                    } else {
                        uiState.sections.forEach { section ->
                            item(key = "header-${section.title}") {
                                GroupHeader(title = section.title, count = section.items.size)
                            }
                            itemsIndexed(
                                section.items,
                                key = { _, row -> "${row.result.sessionId}-${row.result.athleteId}" },
                            ) { index, row ->
                                val rank = if ((row.result.duration ?: 0L) > 0L) index + 1 else null
                                ResultTableRow(rank, row.result, row.athlete)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyResultsCard(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ResultMetric(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GroupHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "$count ${if (count == 1) "athlete" else "athletes"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "PLACE",
            modifier = Modifier.width(42.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "ATHLETE",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "TIME",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultTableRow(rank: Int?, result: SessionResult, athlete: Athlete?) {
    val isFinished = (result.duration ?: 0L) > 0L
    val rankContainer = when (rank) {
        1 -> MaterialTheme.colorScheme.secondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val rankContent = when (rank) {
        1 -> MaterialTheme.colorScheme.onSecondaryContainer
        2 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val statusText = when {
        isFinished -> "Finished"
        result.startedAt != null -> "In progress"
        else -> "No time"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = rankContainer,
            modifier = Modifier.size(34.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = rank?.toString() ?: "-",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = rankContent,
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = athlete?.name ?: "Athlete #${result.athleteId}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Start ${result.startOrder} | ${result.distance} m",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = result.duration?.takeIf { it > 0L }?.let(Utils::formatDuration) ?: "--",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isFinished) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
