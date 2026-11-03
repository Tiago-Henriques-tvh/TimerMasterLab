package com.tiagohenriques.project.timermasterlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.AppContainer
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.platform.shareFile
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private data class ResultRow(
    val result: SessionResult,
    val athlete: Athlete?,
)

@Composable
fun SessionHistoryScreen(
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val sessions by container.sessionRepository.getAllTrialSessions().collectAsState(initial = emptyList())
    val results by container.sessionResultRepository.getAllSessionResults().collectAsState(initial = emptyList())
    val athletes by container.athletesRepository.getAllAthletes().collectAsState(initial = emptyList())
    val orderedSessions = sessions.sortedByDescending(::sessionTimestamp)
    val scope = rememberCoroutineScope()
    var selectedSessionId by remember { mutableStateOf<Long?>(null) }
    var grouping by remember { mutableStateOf("None") }
    var sessionMenuExpanded by remember { mutableStateOf(false) }
    var groupingMenuExpanded by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val selectedSession = orderedSessions.firstOrNull { it.sessionId == selectedSessionId }
    val selectedResults = results
        .filter { it.sessionId == selectedSession?.sessionId && (it.duration ?: 0L) > 0L }
        .map { result -> ResultRow(result, athletes.firstOrNull { it.id == result.athleteId }) }
        .sortedBy { it.result.duration }
    val sections = buildResultSections(selectedResults, grouping)
    val pdfBytes = remember(selectedSession, sections, grouping) {
        selectedSession?.let {
            createSessionResultsPdf(
                title = "Session Results - ${sessionDateLabel(it)}",
                sections = sections.map { section -> section.title to section.rows.map { it.result to it.athlete } },
            )
        }
    }

    val pdfSaver = rememberFileSaverLauncher { file ->
        if (file != null && pdfBytes != null) {
            scope.launch {
                try {
                    file.write(pdfBytes)
                    feedback = "PDF exported."
                } catch (exception: Exception) {
                    feedback = exception.message ?: "Could not save the PDF file."
                }
            }
        }
    }

    if (orderedSessions.isEmpty()) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "🏁 Session Results",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text("0", color = MaterialTheme.colorScheme.primary)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Completed races will appear here.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        return
    }

    LaunchedSessionDefault(orderedSessions, selectedSessionId) { selectedSessionId = it }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    Text(
                        "🏁 Session Results",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    ) {
                        Text(
                            "${selectedResults.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { sessionMenuExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Select session")
                    Spacer(Modifier.width(8.dp))
                    Text(selectedSession?.let(::sessionDateLabel) ?: "Select a session")
                }
                DropdownMenu(
                    expanded = sessionMenuExpanded,
                    onDismissRequest = { sessionMenuExpanded = false },
                ) {
                    orderedSessions.forEach { session ->
                        DropdownMenuItem(
                            text = { Text(sessionDateLabel(session)) },
                            onClick = {
                                selectedSessionId = session.sessionId
                                feedback = null
                                sessionMenuExpanded = false
                            },
                        )
                    }
                }
            }
        }

        if (selectedSession == null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "Select a session to view results.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Column
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.weight(1f),
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    OutlinedButton(onClick = { groupingMenuExpanded = true }) {
                        Icon(Icons.Default.FilterAlt, contentDescription = "Group")
                        Spacer(Modifier.width(6.dp))
                        Text(grouping)
                    }
                    DropdownMenu(
                        expanded = groupingMenuExpanded,
                        onDismissRequest = { groupingMenuExpanded = false },
                    ) {
                        GROUP_OPTIONS.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    grouping = option
                                    groupingMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
            FilledTonalButton(
                enabled = pdfBytes != null && selectedResults.isNotEmpty(),
                onClick = {
                    pdfSaver.launch(
                        suggestedName = "session_results_${selectedSession.sessionId}",
                        extension = "pdf",
                    )
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = "Export")
                Spacer(Modifier.width(6.dp))
                Text("Export")
            }
            IconButton(
                enabled = pdfBytes != null && selectedResults.isNotEmpty(),
                onClick = {
                    val bytes = pdfBytes ?: return@IconButton
                    scope.launch {
                        try {
                            shareFile(
                                name = "session_results_${selectedSession.sessionId}.pdf",
                                bytes = bytes,
                                mimeType = "application/pdf",
                            )
                            feedback = null
                        } catch (exception: Exception) {
                            feedback = exception.message ?: "Could not share the PDF."
                        }
                    }
                },
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share PDF")
            }
        }

        feedback?.let { message ->
            Text(
                message,
                color = if (message.startsWith("Could not")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
        }
        if (selectedResults.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "No completed results yet.",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (grouping == "None") {
                item { ResultTableHeader() }
                itemsIndexed(
                    sections.firstOrNull()?.rows.orEmpty(),
                    key = { _, row -> "${row.result.sessionId}-${row.result.athleteId}" },
                ) { index, row ->
                    ResultTableRow(row, isAlternate = index % 2 == 1)
                }
            } else {
                sections.forEach { section ->
                    item(key = "header-${section.title}") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        ) {
                            Text(
                                section.title,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                "${section.rows.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        ResultTableHeader()
                    }
                    itemsIndexed(
                        section.rows,
                        key = { _, row -> "${row.result.sessionId}-${row.result.athleteId}" },
                    ) { index, row ->
                        ResultTableRow(row, isAlternate = index % 2 == 1)
                    }
                }
            }
        }
    }
}

private data class ResultSection(
    val title: String,
    val rows: List<ResultRow>,
)

private fun buildResultSections(rows: List<ResultRow>, grouping: String): List<ResultSection> {
    if (grouping == "None") return listOf(ResultSection("", rows))
    val grouped = rows.groupBy { row ->
        when (grouping) {
            "Age" -> row.athlete?.age?.toString() ?: "Unknown"
            "Gender" -> row.athlete?.gender ?: "Unknown"
            "Category" -> row.athlete?.let { Category.fromCode(it.category) }?.ifBlank { "Unknown" } ?: "Unknown"
            "Distance" -> row.result.distance.toString()
            else -> "Unknown"
        }
    }
    val keys = grouped.keys.sortedWith(
        compareBy<String> { it.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it.lowercase() },
    )
    return keys.map { key -> ResultSection(key, grouped.getValue(key)) }
}

@Composable
private fun ResultTableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            "Start",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Name",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Time",
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ResultTableRow(row: ResultRow, isAlternate: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(
                color = if (isAlternate) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(8.dp),
            )
            .padding(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            "${row.result.startOrder}.",
            modifier = Modifier.width(48.dp),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            row.athlete?.name ?: "Athlete #${row.result.athleteId}",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            formatResultDuration(row.result.duration),
            modifier = Modifier.width(96.dp),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LaunchedSessionDefault(
    sessions: List<Session>,
    selectedSessionId: Long?,
    onSelect: (Long) -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(sessions, selectedSessionId) {
        if (sessions.none { it.sessionId == selectedSessionId }) {
            sessions.firstOrNull()?.let { onSelect(it.sessionId) }
        }
    }
}

private fun sessionDateLabel(session: Session): String =
    try {
        val instant = session.date.toLongOrNull()?.let(Instant::fromEpochMilliseconds)
            ?: Instant.parse(session.date)
        val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.year.toString().padStart(4, '0')}-${local.monthNumber.toString().padStart(2, '0')}-" +
            "${local.dayOfMonth.toString().padStart(2, '0')} ${local.hour.toString().padStart(2, '0')}:" +
            local.minute.toString().padStart(2, '0')
    } catch (_: IllegalArgumentException) {
        "Session #${session.sessionId}"
    }

private fun sessionTimestamp(session: Session): Long =
    session.date.toLongOrNull() ?: try {
        Instant.parse(session.date).toEpochMilliseconds()
    } catch (_: IllegalArgumentException) {
        0L
    }

private fun formatResultDuration(durationMillis: Long?): String {
    if (durationMillis == null || durationMillis <= 0L) return "--:--:--"
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.toString().padStart(2, '0')}.${minutes.toString().padStart(2, '0')}." +
        seconds.toString().padStart(2, '0')
}

private val GROUP_OPTIONS = listOf("None", "Age", "Gender", "Category", "Distance")
