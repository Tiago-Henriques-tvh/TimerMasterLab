package com.tiagohenriques.project.timermasterlab.ui.groupRace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.components.AthleteResultRow
import com.tiagohenriques.project.timermasterlab.ui.components.FinishedIcon
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceActionButton
import com.tiagohenriques.project.timermasterlab.ui.components.LiveRaceStatus
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusColor
import com.tiagohenriques.project.timermasterlab.ui.components.liveRaceStatusLabel
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.subScreens.DraftGroupRaceItem
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatLiveDuration

@Composable
fun GroupRaceBoard(
    draft: List<DraftGroupRaceItem>,
    nowMillis: Long,
    split: Boolean,
    onStart: (Int) -> Unit,
    onStop: (DraftGroupRaceItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalUiMetrics.current
    val grouped = draft.sortedBy { it.startOrder }.groupBy { it.startOrder }
    val waiting = grouped.filter { (_, athletes) -> athletes.none { it.result?.startedAt != null } }
    val active = grouped.filter { (_, athletes) -> athletes.any { it.result?.startedAt != null } }
    if (split) {
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(metrics.gap)) {
            GroupColumn("Start queue", waiting, nowMillis, onStart, onStop, Modifier.weight(1f).fillMaxHeight())
            GroupColumn("Finish line", active, nowMillis, onStart, onStop, Modifier.weight(1.1f).fillMaxHeight())
        }
    } else {
        GroupColumn("Groups", grouped, nowMillis, onStart, onStop, modifier.fillMaxSize())
    }
}

@Composable
private fun GroupColumn(
    title: String,
    groups: Map<Int, List<DraftGroupRaceItem>>,
    nowMillis: Long,
    onStart: (Int) -> Unit,
    onStop: (DraftGroupRaceItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalUiMetrics.current
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(metrics.gap)) {
        Text(text = title, style = metrics.title)
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(metrics.gap)) {
            if (groups.isEmpty()) {
                item(key = "empty-$title") {
                    Text(
                        text = "No groups here",
                        style = metrics.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            groups.forEach { (groupOrder, athletes) ->
                item(key = "$title-$groupOrder") {
                    GroupCard(groupOrder, athletes, nowMillis, { onStart(groupOrder) }, onStop)
                }
            }
        }
    }
}

@Composable
private fun GroupCard(
    groupOrder: Int,
    athletes: List<DraftGroupRaceItem>,
    nowMillis: Long,
    onStart: () -> Unit,
    onStop: (DraftGroupRaceItem) -> Unit,
) {
    val metrics = LocalUiMetrics.current
    val started = athletes.any { it.result?.startedAt != null }
    val finished = athletes.all { (it.result?.duration ?: 0L) > 0L }
    val status = when {
        finished -> LiveRaceStatus.Finished
        started -> LiveRaceStatus.OnCourse
        else -> LiveRaceStatus.Queued
    }
    Card(
        shape = RoundedCornerShape(metrics.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(metrics.gap),
            verticalArrangement = Arrangement.spacedBy(metrics.gap),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(metrics.gap),
            ) {
                Text(
                    text = "Group $groupOrder",
                    style = metrics.title,
                    color = liveRaceStatusColor(status),
                    modifier = Modifier.weight(1f),
                )
                if (!started) {
                    LiveRaceActionButton(label = "Start", containerColor = Color(0xFF2E7D32), onClick = onStart)
                } else {
                    Text(
                        text = liveRaceStatusLabel(status),
                        style = metrics.body,
                        color = liveRaceStatusColor(status),
                    )
                }
            }
            athletes.forEach { item ->
                GroupAthleteRow(item, nowMillis) { onStop(item) }
            }
        }
    }
}

@Composable
private fun GroupAthleteRow(
    item: DraftGroupRaceItem,
    nowMillis: Long,
    onStop: () -> Unit,
) {
    val metrics = LocalUiMetrics.current
    val startedAt = item.result?.startedAt
    val duration = item.result?.duration
    val running = startedAt != null && (duration == null || duration == 0L)
    val finished = duration != null && duration > 0L
    val clock = when {
        running -> formatLiveDuration(nowMillis - startedAt)
        finished -> formatLiveDuration(duration)
        else -> "--:--"
    }
    AthleteResultRow(
        startOrder = item.startOrder,
        athleteName = item.athlete.name,
        durationText = clock,
        rowHeight = metrics.buttonHeight + 12.dp,
        actionWidth = metrics.buttonWidth,
    ) {
        when {
            finished -> FinishedIcon()
            running -> LiveRaceActionButton(label = "Stop", containerColor = Color(0xFFC62828), onClick = onStop)
            else -> Text(
                text = "Wait",
                style = metrics.body,
                color = liveRaceStatusColor(LiveRaceStatus.Queued),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
