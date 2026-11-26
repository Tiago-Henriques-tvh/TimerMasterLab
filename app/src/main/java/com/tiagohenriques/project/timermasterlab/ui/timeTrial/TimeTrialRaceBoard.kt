package com.tiagohenriques.project.timermasterlab.ui.timeTrial

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceActionButton
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceStatus
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusColor
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusLabel
import com.tiagohenriques.project.timermasterlab.ui.components.pressScale
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.responsive.WindowSize
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
    onIgnore: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gap = LocalUiMetrics.current.gap
    val finishLine = running.sortedBy { it.startOrder } + finished.sortedBy { it.startOrder }

    if (split) {
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
            QueuePane(
                queued = queued,
                onStart = onStart,
                onIgnore = onIgnore,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            FinishPane(
                athletes = finishLine,
                nowMillis = nowMillis,
                onStop = onStop,
                modifier = Modifier.weight(1.1f).fillMaxHeight(),
            )
        }
    } else {
        Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            QueuePane(
                queued = queued,
                onStart = onStart,
                onIgnore = onIgnore,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            FinishPane(
                athletes = finishLine,
                nowMillis = nowMillis,
                onStop = onStop,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun QueuePane(
    queued: List<DraftTimeTrialItem>,
    onStart: (DraftTimeTrialItem) -> Unit,
    onIgnore: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    RacePane(
        title = "Next to start",
        count = queued.size,
        status = LiveRaceStatus.Queued,
        emptyText = "No athletes waiting",
        modifier = modifier,
    ) {
        items(queued, key = { "q-${it.athlete.id}" }) { item ->
            val isNext = item.athlete.id == queued.firstOrNull()?.athlete?.id
            AthleteRaceRow(
                item = item,
                detail = "${if (isNext) "Next" else "Queued"} · ${item.distance.toInt()} m",
                status = LiveRaceStatus.Queued,
                onStart = { onStart(item) },
                onStop = {},
                onIgnore = { onIgnore(item) },
            )
        }
    }
}

@Composable
private fun FinishPane(
    athletes: List<DraftTimeTrialItem>,
    nowMillis: Long,
    onStop: (DraftTimeTrialItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val runningIds = athletes
        .filter { (it.result?.duration ?: 0L) == 0L }
        .mapTo(mutableSetOf()) { it.athlete.id }
    val runningCount = runningIds.size
    val status = if (runningCount > 0) LiveRaceStatus.OnCourse else LiveRaceStatus.Finished

    RacePane(
        title = "Finish line",
        count = athletes.size,
        status = status,
        emptyText = "No athletes on course",
        modifier = modifier,
    ) {
        items(
            athletes,
            key = {
                // Changing keys prevents LazyColumn from following a finished row to the end.
                val state = if (it.athlete.id in runningIds) "running" else "finished"
                "f-$state-${it.athlete.id}"
            },
        ) { item ->
            val isRunning = item.athlete.id in runningIds
            val startedAt = item.result?.startedAt ?: nowMillis
            val clock = if (isRunning) {
                formatLiveDuration(nowMillis - startedAt)
            } else {
                formatLiveDuration(item.result?.duration)
            }
            AthleteRaceRow(
                item = item,
                detail = if (isRunning) {
                    "${liveRaceStatusLabel(LiveRaceStatus.OnCourse)} · $clock"
                } else {
                    "${liveRaceStatusLabel(LiveRaceStatus.Finished)} · $clock"
                },
                status = if (isRunning) LiveRaceStatus.OnCourse else LiveRaceStatus.Finished,
                onStart = {},
                onStop = { onStop(item) },
                onIgnore = {},
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
    val paneGap = if (LocalWindowSize.current.widthClass == WindowSize.WidthClass.Compact) 1.dp else 4.dp
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(paneGap)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = count.toString(),
                style = metrics.label,
                color = liveRaceStatusColor(status),
            )
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(paneGap),
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
private fun AthleteRaceRow(
    item: DraftTimeTrialItem,
    detail: String,
    status: LiveRaceStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onIgnore: () -> Unit,
) {
    val metrics = LocalUiMetrics.current
    val interaction = remember { MutableInteractionSource() }
    val isQueued = status == LiveRaceStatus.Queued
    val isRunning = status == LiveRaceStatus.OnCourse
    Card(
        onClick = {
            when (status) {
                LiveRaceStatus.Queued -> onStart()
                LiveRaceStatus.OnCourse -> onStop()
                LiveRaceStatus.Finished -> Unit
            }
        },
        enabled = status != LiveRaceStatus.Finished,
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .pressScale(interaction),
        shape = RoundedCornerShape(metrics.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OrderBadge(item.startOrder, liveRaceStatusColor(status))
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.athlete.name,
                    style = metrics.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = detail,
                    style = metrics.body,
                    color = liveRaceStatusColor(status),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (isQueued) {
                IconButton(
                    onClick = onIgnore,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Ignore ${item.athlete.name}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Spacer(Modifier.width(48.dp))
            }

            Box(
                modifier = Modifier.width(metrics.buttonWidth),
                contentAlignment = Alignment.CenterEnd,
            ) {
                when {
                    isQueued -> LiveRaceActionButton(
                        label = "Start",
                        containerColor = Color(0xFF2E7D32),
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    isRunning -> LiveRaceActionButton(
                        label = "Stop",
                        containerColor = Color(0xFFB63F3A),
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    else -> Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        shape = RoundedCornerShape(metrics.cardRadius),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = "Done", style = metrics.label, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderBadge(order: Int, accent: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = order.toString(),
            style = LocalUiMetrics.current.label,
            color = Color.White,
            maxLines = 1,
        )
    }
}
