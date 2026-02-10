package com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Level
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.TrialSession
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun SessionResultsScreen(navController: NavHostController) {
    val viewModel: SessionResultViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val sessions by viewModel.getAllTrialSessions().collectAsState(initial = emptyList())
    var selectedSession by remember { mutableStateOf<TrialSession?>(null) }

    var expanded by remember { mutableStateOf(false) }
    val sessionResults = selectedSession?.let { session ->
        viewModel.getSessionResultsBySessionId(session.sessionId)
            .collectAsState(initial = emptyList())
    }?.value ?: emptyList()

    val groupOptions = listOf("None", "Age", "Gender", "Level", "Distance")
    var selectedGroup by remember { mutableStateOf("None") }
    var groupExpanded by remember { mutableStateOf(false) }

    val athleteIds = sessionResults.map { it.athleteId }.distinct()
    val athleteMap: Map<Long, Athlete?> = athleteIds.associateWith { id ->
        try {
            runBlocking { viewModel.getAthleteById(id).first() }
        } catch (_: Throwable) {
            null
        }
    }

    val enriched: List<Pair<SessionResult, Athlete?>> = sessionResults.map { result ->
        result to athleteMap[result.athleteId]
    }

    val resultsSorted = enriched.sortedBy { (result, _) -> result.duration ?: Long.MAX_VALUE }

    val finalResults: List<Pair<SessionResult, Athlete?>> = if (selectedGroup == "None") {
        resultsSorted
    } else {
        val grouped = resultsSorted.groupBy { (result, athlete) ->
            when (selectedGroup) {
                "Age" -> athlete?.age?.toString() ?: "Unknown"
                "Gender" -> athlete?.gender ?: "Unknown"
                "Level" -> athlete?.let { Level.fromCode(it.level) } ?: "Unknown"
                "Distance" -> result.distance.toString()
                else -> "Unknown"
            }
        }

        grouped.toSortedMap().flatMap { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏁 Session Results",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )

                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${finalResults.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Icon(
                        Icons.Filled.FilterList,
                        contentDescription = "Select session",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        selectedSession?.let { s ->
                            Utils.formatSessionDate(s.date).takeIf { it.isNotBlank() }
                                ?: "Session #${s.sessionId}"
                        } ?: "Select a session",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    sessions.forEach { session ->
                        val label = Utils.formatSessionDate(session.date).takeIf { it.isNotBlank() }
                            ?: "Session #${session.sessionId}"
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            selectedSession = session
                            expanded = false
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedSession == null) {
            EmptyState()
        } else {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Group:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(onClick = { groupExpanded = true }) {
                            Icon(
                                Icons.Filled.FilterList,
                                contentDescription = "Group",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(selectedGroup)
                        }
                        DropdownMenu(expanded = groupExpanded, onDismissRequest = { groupExpanded = false }) {
                            groupOptions.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = {
                                    selectedGroup = g
                                    groupExpanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                val context = LocalContext.current
                FilledTonalButton(
                    onClick = {
                        try {
                            viewModel.exportResultsToPdf(context, finalResults, selectedGroup)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.FileDownload,
                        contentDescription = "Export",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Export", color = MaterialTheme.colorScheme.onPrimary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedGroup == "None") {
                    item { ResultTableHeader() }
                    itemsIndexed(finalResults) { index, pair ->
                        val (result, athlete) = pair
                        ResultTableRow(result, athlete, isAlternate = index % 2 == 1)
                    }
                } else {
                    val grouped = finalResults.groupBy { (result, athlete) ->
                        when (selectedGroup) {
                            "Age" -> athlete?.age?.toString() ?: "Unknown"
                            "Gender" -> athlete?.gender ?: "Unknown"
                            "Level" -> athlete?.let { Level.fromCode(it.level) } ?: "Unknown"
                            "Distance" -> result.distance.toString()
                            else -> "Unknown"
                        }
                    }.toSortedMap()

                    grouped.forEach { (groupKey, itemsInGroup) ->
                        item {
                            GroupHeader(title = groupKey, count = itemsInGroup.size)
                            Spacer(modifier = Modifier.height(6.dp))
                            ResultTableHeader()
                        }

                        itemsIndexed(itemsInGroup) { localIndex, pair ->
                            val (result, athlete) = pair
                            ResultTableRow(result, athlete, isAlternate = localIndex % 2 == 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "👆",
            style = MaterialTheme.typography.displayMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Please select a session to see results",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Suppress("unused")
@Composable
private fun ResultCardMinimal(result: SessionResult, athlete: Athlete?) {
    StyledResultRow(result, athlete)
}

@Composable
private fun StyledResultRow(result: SessionResult, athlete: Athlete?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${result.startOrder}",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = athlete?.name ?: "Athlete #${result.athleteId}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Distance: ${result.distance} m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = Utils.formatDuration(result.duration),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun GroupHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = "  $title  ",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        ) {
            Text(
                text = " $count ",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ResultTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(color = MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Pos",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Name",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Time",
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ResultTableRow(result: SessionResult, athlete: Athlete?, isAlternate: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = if (isAlternate) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // position
        Text(
            text = "${result.startOrder}.",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        // name
        Text(
            text = athlete?.name ?: "Athlete #${result.athleteId}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // time
        Text(
            text = Utils.formatDuration(result.duration),
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
