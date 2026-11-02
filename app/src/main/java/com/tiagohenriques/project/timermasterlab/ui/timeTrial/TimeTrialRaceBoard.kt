package com.tiagohenriques.project.timermasterlab.ui.timeTrial

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceActionButton
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceStatus
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusColor
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusLabel
import com.tiagohenriques.project.timermasterlab.ui.components.pressScale
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.subScreens.DraftTimeTrialItem
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatLiveDuration

@Composable
fun TimeTrialRaceBoard(
    queued: List<DraftTimeTrialItem>,
    running: List<DraftTimeTrialItem>,
    finished: List<DraftTimeTrialItem>,
    nowMillis: Long,
    split: Boolean,
    onStart: (DraftTimeTrialItem) -> Unit,
    onStop: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = LocalUiMetrics.current.gap
    if (split) {
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            QueuePane(queued, onStart, Modifier.weight(1f).fillMaxHeight())
            LivePane(running, finished, nowMillis, onStop, Modifier.weight(1.1f).fillMaxHeight())
        }
    } else {
        Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(gap)) {
            QueuePane(queued, onStart, Modifier.weight(1f).fillMaxWidth())
            LivePane(running, finished, nowMillis, onStop, Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun QueuePane(
    queued: List<DraftTimeTrialItem>,
    onStart: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    RacePane("Start queue", queued.size, LiveRaceStatus.Queued, "No athletes waiting", modifier) {
        items(queued, key = { "q-${it.athlete.id}" }) { item ->
            val next = item.athlete.id == queued.firstOrNull()?.athlete?.id
            AthleteRaceCard(
                name = item.athlete.name,
                order = item.startOrder,
                detail = if (next) "Next" else "Queued",
                clock = "${item.distance.toInt()} m",
                status = LiveRaceStatus.Queued,
                actionLabel = "Start",
                actionColor = Color(0xFF2E7D32),
                onAction = { onStart(item) },
            )
        }
    }
}

@Composable
private fun LivePane(
    running: List<DraftTimeTrialItem>,
    finished: List<DraftTimeTrialItem>,
    nowMillis: Long,
    onStop: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = if (running.isNotEmpty()) LiveRaceStatus.OnCourse else LiveRaceStatus.Finished
    RacePane("Finish line", running.size + finished.size, status, "No athletes on course", modifier) {
        items(running, key = { "r-${it.athlete.id}" }) { item ->
            val startedAt = item.result?.startedAt ?: nowMillis
            AthleteRaceCard(
                name = item.athlete.name,
                order = item.startOrder,
                detail = liveRaceStatusLabel(LiveRaceStatus.OnCourse),
                clock = formatLiveDuration(nowMillis - startedAt),
                status = LiveRaceStatus.OnCourse,
                actionLabel = "Stop",
                actionColor = Color(0xFFC62828),
                onAction = { onStop(item) },
            )
        }
        items(finished, key = { "f-${it.athlete.id}" }) { item ->
            AthleteRaceCard(
                name = item.athlete.name,
                order = item.startOrder,
                detail = liveRaceStatusLabel(LiveRaceStatus.Finished),
                clock = formatLiveDuration(item.result?.duration),
                status = LiveRaceStatus.Finished,
                actionLabel = null,
                actionColor = Color(0xFF2E7D32),
                onAction = {},
            )
        }
    }
}

@Composable
private fun RacePane(
    title: String,
    count: Int,
    status: LiveRaceStatus,
    emptyText: String,
    modifier: Modifier = Modifier,
    rows: LazyListScope.() -> Unit,
) {
    val metrics = LocalUiMetrics.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(metrics.gap)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = metrics.title, modifier = Modifier.weight(1f))
            Text(text = count.toString(), style = metrics.label, color = liveRaceStatusColor(status))
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(metrics.gap),
        ) {
            if (count == 0) {
                item(key = "empty-$title") {
                    Text(
                        text = emptyText,
                        style = metrics.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            rows()
        }
    }
}

@Composable
private fun AthleteRaceCard(
    name: String,
    order: Int,
    detail: String,
    clock: String,
    status: LiveRaceStatus,
    actionLabel: String?,
    actionColor: Color,
    onAction: () -> Unit,
) {
    val metrics = LocalUiMetrics.current
    val interaction = remember { MutableInteractionSource() }
    val accent by animateColorAsState(liveRaceStatusColor(status), label = "status")
    Card(
        onClick = { if (actionLabel != null) onAction() },
        enabled = actionLabel != null,
        interactionSource = interaction,
        modifier = Modifier.fillMaxWidth().pressScale(interaction),
        shape = RoundedCornerShape(metrics.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = order.toString(),
                style = metrics.label,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(metrics.icon + 6.dp)
                    .background(accent, CircleShape)
                    .padding(top = 3.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = metrics.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = detail, style = metrics.body, color = accent, maxLines = 1)
            }
            Text(text = clock, style = metrics.label, maxLines = 1)
            if (actionLabel != null) {
                LiveRaceActionButton(label = actionLabel, containerColor = actionColor, onClick = onAction)
            }
        }
    }
}
