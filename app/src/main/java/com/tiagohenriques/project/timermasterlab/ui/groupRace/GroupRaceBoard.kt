package com.tiagohenriques.project.timermasterlab.ui.groupRace

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.tiagohenriques.project.timermasterlab.ui.components.pressScale
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalWindowSize
import com.tiagohenriques.project.timermasterlab.ui.responsive.WindowSize
import com.tiagohenriques.project.timermasterlab.ui.subScreens.DraftGroupRaceItem
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils.Companion.formatLiveDuration

private data class GroupRaceRow(
    val item: DraftGroupRaceItem,
    val isGroupLeader: Boolean,
)

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
    val waiting = grouped.filterValues { athletes -> athletes.none { it.result?.startedAt != null } }
    val active = grouped.filterValues { athletes -> athletes.any { it.result?.startedAt != null } }

    if (split) {
        Row(modifier = modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(metrics.gap)) {
            GroupRacePane(
                title = "Next to start",
                groups = waiting,
                nowMillis = nowMillis,
                isStartQueue = true,
                onStart = onStart,
                onStop = onStop,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
            GroupRacePane(
                title = "Finish line",
                groups = active,
                nowMillis = nowMillis,
                isStartQueue = false,
                onStart = onStart,
                onStop = onStop,
                modifier = Modifier.weight(1.1f).fillMaxHeight(),
            )
        }
    } else {
        val paneGap = 1.dp
        Column(modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(paneGap)) {
            GroupRacePane(
                title = "Next to start",
                groups = waiting,
                nowMillis = nowMillis,
                isStartQueue = true,
                onStart = onStart,
                onStop = onStop,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
            GroupRacePane(
                title = "Finish line",
                groups = active,
                nowMillis = nowMillis,
                isStartQueue = false,
                onStart = onStart,
                onStop = onStop,
                modifier = Modifier.weight(1f).fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun GroupRacePane(
    title: String,
    groups: Map<Int, List<DraftGroupRaceItem>>,
    nowMillis: Long,
    isStartQueue: Boolean,
    onStart: (Int) -> Unit,
    onStop: (DraftGroupRaceItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metrics = LocalUiMetrics.current
    val compact = LocalWindowSize.current.widthClass == WindowSize.WidthClass.Compact
    val gap = if (compact) 1.dp else 4.dp
    val athletes = groups.toSortedMap().flatMap { (_, groupAthletes) ->
        groupAthletes.mapIndexed { index, athlete ->
            GroupRaceRow(item = athlete, isGroupLeader = index == 0)
        }
    }
    val paneStatus = when {
        isStartQueue -> LiveRaceStatus.Queued
        athletes.any { (it.item.result?.duration ?: 0L) == 0L } -> LiveRaceStatus.OnCourse
        else -> LiveRaceStatus.Finished
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
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
                text = "${athletes.size}",
                style = metrics.label,
                color = liveRaceStatusColor(paneStatus),
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            if (athletes.isEmpty()) {
                item(key = "empty-$title") {
                    Text(
                        text = if (isStartQueue) "No athletes waiting" else "No athletes on course",
                        style = metrics.body,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(athletes, key = { it.item.athlete.id }) { row ->
                GroupRaceAthleteRow(
                    row = row,
                    nowMillis = nowMillis,
                    isStartQueue = isStartQueue,
                    onStart = { onStart(row.item.startOrder) },
                    onStop = { onStop(row.item) },
                )
            }
        }
    }
}

@Composable
private fun GroupRaceAthleteRow(
    row: GroupRaceRow,
    nowMillis: Long,
    isStartQueue: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val metrics = LocalUiMetrics.current
    val item = row.item
    val startedAt = item.result?.startedAt
    val duration = item.result?.duration
    val running = startedAt != null && (duration == null || duration == 0L)
    val finished = duration != null && duration > 0L
    val status = when {
        running -> LiveRaceStatus.OnCourse
        finished -> LiveRaceStatus.Finished
        else -> LiveRaceStatus.Queued
    }
    val detail = when {
        isStartQueue -> "Group ${item.startOrder} · ${item.distance.toInt()} m"
        running -> "Group ${item.startOrder} · ${formatLiveDuration(nowMillis - (startedAt ?: nowMillis))}"
        finished -> "Group ${item.startOrder} · ${formatLiveDuration(duration)}"
        else -> "Group ${item.startOrder} · Waiting"
    }
    val interaction = remember { MutableInteractionSource() }

    Card(
        onClick = {
            when {
                isStartQueue && row.isGroupLeader -> onStart()
                !isStartQueue && running -> onStop()
            }
        },
        enabled = (isStartQueue && row.isGroupLeader) || (!isStartQueue && running),
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
            GroupBadge(
                item.startOrder,
                liveRaceStatusColor(status),
                contentColor = if (status == LiveRaceStatus.Queued) {
                    MaterialTheme.colorScheme.onTertiary
                } else {
                    Color.White
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            ) {
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

            Box(
                modifier = Modifier.width(120.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                when {
                    isStartQueue && row.isGroupLeader -> LiveRaceActionButton(
                        label = "Start group",
                        containerColor = MaterialTheme.colorScheme.primary,
                        onClick = onStart,
                        modifier = Modifier.fillMaxWidth(),
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                    isStartQueue -> StatusSlot(text = "Together")
                    running -> LiveRaceActionButton(
                        label = "Stop",
                        containerColor = Color(0xFFB63F3A),
                        onClick = onStop,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    finished -> StatusSlot(text = "Done", isFinished = true)
                    else -> StatusSlot(text = "Waiting")
                }
            }
        }
    }
}

@Composable
private fun StatusSlot(text: String, isFinished: Boolean = false) {
    val metrics = LocalUiMetrics.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp),
        shape = RoundedCornerShape(metrics.cardRadius),
        color = if (isFinished) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        contentColor = if (isFinished) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = metrics.label, maxLines = 1)
        }
    }
}

@Composable
private fun GroupBadge(groupOrder: Int, accent: Color, contentColor: Color) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(accent, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = groupOrder.toString(),
            style = LocalUiMetrics.current.label,
            color = contentColor,
            maxLines = 1,
        )
    }
}
