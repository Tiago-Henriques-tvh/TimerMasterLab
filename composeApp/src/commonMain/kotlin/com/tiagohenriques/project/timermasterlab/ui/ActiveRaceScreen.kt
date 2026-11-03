package com.tiagohenriques.project.timermasterlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.AppContainer
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.platform.currentTimeMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ActiveRaceScreen(
    mode: Type,
    container: AppContainer,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var sessionId by remember(mode) { mutableStateOf<Long?>(null) }
    var sessionStartedAt by remember(mode) { mutableStateOf<Long?>(null) }
    var sessionCompletedAt by remember(mode) { mutableStateOf<Long?>(null) }
    var athletes by remember { mutableStateOf(emptyMap<Long, Athlete>()) }
    var nowMillis by remember { mutableLongStateOf(currentTimeMillis()) }
    var busy by remember { mutableStateOf(true) }
    var showAbortConfirmation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val resultsFlow = remember(sessionId) {
        sessionId?.let(container.sessionResultRepository::getSessionResultsBySessionId) ?: emptyFlow()
    }
    val results by resultsFlow.collectAsState(initial = emptyList())
    val isComplete = results.isNotEmpty() && results.all { it.duration != null }
    val sessionElapsed = sessionStartedAt?.let {
        ((sessionCompletedAt ?: nowMillis) - it).coerceAtLeast(0L)
    } ?: 0L

    LaunchedEffect(mode) {
        busy = true
        errorMessage = null
        try {
            val entries = when (mode) {
                Type.TIME_TRIAL -> container.startListEntryRepository.getTimeTrialStartList().first()
                Type.GROUP_RACE -> container.startListEntryRepository.getGroupRaceStartList().first()
            }
            check(entries.isNotEmpty()) { "Save a start list before starting a race." }
            athletes = container.athletesRepository.getAllAthletesOnce().associateBy { it.id }
            sessionStartedAt = currentTimeMillis()
            sessionId = when (mode) {
                Type.TIME_TRIAL -> container.sessionRepository.startTimeTrialSession()
                Type.GROUP_RACE -> container.sessionRepository.startGroupRaceSession()
            }
        } catch (exception: Exception) {
            errorMessage = exception.message ?: "Could not start the race."
        } finally {
            busy = false
        }
    }

    LaunchedEffect(sessionId) {
        if (sessionId != null) {
            while (true) {
                nowMillis = currentTimeMillis()
                delay(50)
            }
        }
    }

    LaunchedEffect(sessionId, isComplete) {
        if (sessionId != null && isComplete) {
            if (sessionCompletedAt == null) sessionCompletedAt = currentTimeMillis()
            try {
                container.sessionRepository.pruneCompletedSessions()
            } catch (exception: Exception) {
                errorMessage = exception.message ?: "Could not clean up older completed sessions."
            }
        }
    }

    val queued = results.filter { it.startedAt == null }.sortedBy { it.startOrder }
    val inProgress = results.filter { it.startedAt != null && it.duration == null }
    val finished = results.filter { it.duration != null }.sortedBy { it.duration }

    BoxWithConstraints(modifier = modifier) {
        val split = maxWidth >= 600.dp
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp),
                )
                Text(
                    formatRaceDuration(sessionElapsed),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                if (sessionId != null) {
                    Button(
                        onClick = {
                            if (isComplete) onExit() else showAbortConfirmation = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isComplete) {
                                Color(0xFF2E7D32)
                            } else {
                                Color(0xFFC62828)
                            },
                        ),
                    ) {
                        Text(if (isComplete) "Done" else "Abort")
                    }
                }
            }
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (sessionId == null) {
                Text(if (busy) "Preparing race…" else errorMessage ?: "The race could not be started.")
                if (!busy) {
                    OutlinedButton(onClick = onExit) { Text("Back") }
                }
            } else {
                if (mode == Type.TIME_TRIAL) {
                    TimeTrialRaceBoard(
                        queued = queued,
                        inProgress = inProgress,
                        finished = finished,
                        athletes = athletes,
                        nowMillis = nowMillis,
                        split = split,
                        onStart = { result ->
                            scope.launch {
                                try {
                                    container.sessionResultRepository.update(
                                        result.copy(startedAt = currentTimeMillis()),
                                    )
                                } catch (exception: Exception) {
                                    errorMessage = exception.message ?: "Could not start the athlete."
                                }
                            }
                        },
                        onStop = { result ->
                            scope.launch {
                                try {
                                    val startedAt = result.startedAt ?: return@launch
                                    val duration = (currentTimeMillis() - startedAt).coerceAtLeast(1L)
                                    container.sessionResultRepository.update(result.copy(duration = duration))
                                } catch (exception: Exception) {
                                    errorMessage = exception.message ?: "Could not stop the athlete."
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    val groups = results.sortedBy { it.startOrder }
                        .groupBy { it.startOrder }
                        .toSortedMap()
                    val waitingGroups = groups.filterValues { group -> group.none { it.startedAt != null } }
                    val activeGroups = groups.filterValues { group -> group.any { it.startedAt != null } }
                    val startGroup: (List<SessionResult>) -> Unit = { groupResults ->
                        busy = true
                        scope.launch {
                            try {
                                val startAt = currentTimeMillis()
                                groupResults.filter { it.startedAt == null }.forEach { result ->
                                    container.sessionResultRepository.update(result.copy(startedAt = startAt))
                                }
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Could not start the group."
                            } finally {
                                busy = false
                            }
                        }
                    }
                    val stopAthlete: (SessionResult) -> Unit = { result ->
                        scope.launch {
                            try {
                                val startedAt = result.startedAt ?: return@launch
                                val duration = (currentTimeMillis() - startedAt).coerceAtLeast(1L)
                                container.sessionResultRepository.update(result.copy(duration = duration))
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Could not stop the athlete."
                            }
                        }
                    }
                    if (split) {
                        Row(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            GroupRacePane(
                                title = "Start queue",
                                groups = waitingGroups,
                                athletes = athletes,
                                nowMillis = nowMillis,
                                busy = busy,
                                onStart = startGroup,
                                onStop = stopAthlete,
                                modifier = Modifier.weight(1f),
                            )
                            GroupRacePane(
                                title = "Finish line",
                                groups = activeGroups,
                                athletes = athletes,
                                nowMillis = nowMillis,
                                busy = busy,
                                onStart = startGroup,
                                onStop = stopAthlete,
                                modifier = Modifier.weight(1.1f),
                            )
                        }
                    } else {
                        GroupRacePane(
                            title = "Groups",
                            groups = groups,
                            athletes = athletes,
                            nowMillis = nowMillis,
                            busy = busy,
                            onStart = startGroup,
                            onStop = stopAthlete,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (isComplete) {
                    Text("Race complete", style = MaterialTheme.typography.titleLarge)
                } else {
                    Text("${queued.size} queued · ${inProgress.size} running · ${finished.size} finished")
                }
            }
        }
    }

    if (showAbortConfirmation) {
        AlertDialog(
            onDismissRequest = { showAbortConfirmation = false },
            title = { Text("Abort race?") },
            text = { Text("The current race and its recorded times will be discarded.") },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        val activeSessionId = sessionId ?: return@TextButton
                        showAbortConfirmation = false
                        busy = true
                        scope.launch {
                            try {
                                container.sessionRepository.delete(
                                    Session(sessionId = activeSessionId, date = ""),
                                )
                                sessionId = null
                                sessionStartedAt = null
                                sessionCompletedAt = null
                                athletes = emptyMap()
                            } catch (exception: Exception) {
                                errorMessage = exception.message ?: "Could not abort the race."
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) {
                    Text("Abort")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbortConfirmation = false }) {
                    Text("Keep racing")
                }
            },
        )
    }
}

@Composable
private fun TimeTrialRaceBoard(
    queued: List<SessionResult>,
    inProgress: List<SessionResult>,
    finished: List<SessionResult>,
    athletes: Map<Long, Athlete>,
    nowMillis: Long,
    split: Boolean,
    onStart: (SessionResult) -> Unit,
    onStop: (SessionResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    val finishLine = inProgress + finished
    if (split) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RaceListPane("Start queue", queued.size, "No athletes waiting", Modifier.weight(1f)) {
                items(queued, key = { "queued-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                        queueLabel = if (result == queued.firstOrNull()) "Next" else "Queued",
                    )
                }
            }
            RaceListPane("Finish line", finishLine.size, "No athletes on course", Modifier.weight(1.1f)) {
                items(inProgress, key = { "running-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                    )
                }
                items(finished, key = { "finished-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            RaceListPane("Start queue", queued.size, "No athletes waiting", Modifier.weight(1f)) {
                items(queued, key = { "queued-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                        queueLabel = if (result == queued.firstOrNull()) "Next" else "Queued",
                    )
                }
            }
            RaceListPane("Finish line", finishLine.size, "No athletes on course", Modifier.weight(1f)) {
                items(inProgress, key = { "running-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                    )
                }
                items(finished, key = { "finished-${it.athleteId}" }) { result ->
                    RaceResultRow(
                        result = result,
                        athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                        nowMillis = nowMillis,
                        onStart = { onStart(result) },
                        onStop = { onStop(result) },
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupRacePane(
    title: String,
    groups: Map<Int, List<SessionResult>>,
    athletes: Map<Long, Athlete>,
    nowMillis: Long,
    busy: Boolean,
    onStart: (List<SessionResult>) -> Unit,
    onStop: (SessionResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    RaceListPane(title, groups.values.sumOf { it.size }, "No groups here", modifier) {
        groups.forEach { (groupOrder, groupResults) ->
            item(key = "$title-$groupOrder") {
                GroupRaceResultsCard(
                    groupOrder = groupOrder,
                    results = groupResults,
                    athletes = athletes,
                    nowMillis = nowMillis,
                    canStart = !busy && groupResults.none { it.startedAt != null },
                    onStart = { onStart(groupResults) },
                    onStop = onStop,
                )
            }
        }
    }
}

@Composable
private fun RaceListPane(
    title: String,
    count: Int,
    emptyText: String,
    modifier: Modifier = Modifier,
    rows: LazyListScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Text(count.toString(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (count == 0) {
                item(key = "empty-$title") {
                    Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            rows()
        }
    }
}

@Composable
private fun RaceResultRow(
    result: SessionResult,
    athleteName: String,
    nowMillis: Long,
    onStart: () -> Unit,
    onStop: () -> Unit,
    canStartIndividually: Boolean = true,
    queueLabel: String = "Queued",
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${result.startOrder}. $athleteName", style = MaterialTheme.typography.titleMedium)
                Text("${result.distance} m")
                val duration = result.duration
                val startedAt = result.startedAt
                when {
                    duration != null -> Text(formatRaceDuration(duration))
                    startedAt != null -> Text(formatRaceDuration(nowMillis - startedAt))
                    else -> Text(queueLabel)
                }
            }
            when {
                result.duration != null -> Unit
                result.                startedAt != null -> Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                ) { Text("Stop") }
                canStartIndividually -> Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                ) { Text("Start") }
                else -> Text("Waiting for group start")
            }
        }
    }
}

@Composable
private fun GroupRaceResultsCard(
    groupOrder: Int,
    results: List<SessionResult>,
    athletes: Map<Long, Athlete>,
    nowMillis: Long,
    canStart: Boolean,
    onStart: () -> Unit,
    onStop: (SessionResult) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Start Group $groupOrder", style = MaterialTheme.typography.titleMedium)
                if (results.none { it.startedAt != null }) {
                    Button(
                        enabled = canStart,
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    ) {
                        Text("Start group")
                    }
                }
            }
            results.sortedBy { it.athleteId }.forEach { result ->
                RaceResultRow(
                    result = result,
                    athleteName = athletes[result.athleteId]?.name ?: "Athlete ${result.athleteId}",
                    nowMillis = nowMillis,
                    onStart = {},
                    onStop = { onStop(result) },
                    canStartIndividually = false,
                )
            }
        }
    }
}

internal fun formatRaceDuration(durationMillis: Long): String {
    val totalTenths = durationMillis.coerceAtLeast(0L) / 100L
    val hours = totalTenths / 36_000
    val minutes = (totalTenths / 600) % 60
    val seconds = (totalTenths / 10) % 60
    val tenths = totalTenths % 10
    return "${hours.toString().padStart(2, '0')}.${minutes.toString().padStart(2, '0')}." +
        "${seconds.toString().padStart(2, '0')}.$tenths"
}
