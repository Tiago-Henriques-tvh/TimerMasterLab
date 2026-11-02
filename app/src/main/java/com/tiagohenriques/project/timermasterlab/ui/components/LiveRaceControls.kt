package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics

enum class LiveRaceStatus {
    Queued,
    OnCourse,
    Finished,
}

fun liveRaceStatusColor(status: LiveRaceStatus): Color = when (status) {
    LiveRaceStatus.Queued -> Color(0xFF42A5F5)
    LiveRaceStatus.OnCourse -> Color(0xFFFFA726)
    LiveRaceStatus.Finished -> Color(0xFF66BB6A)
}

fun liveRaceStatusLabel(status: LiveRaceStatus): String = when (status) {
    LiveRaceStatus.Queued -> "Queued"
    LiveRaceStatus.OnCourse -> "On Course"
    LiveRaceStatus.Finished -> "Finished"
}

@Composable
fun LiveRaceActionButton(
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val metrics = LocalUiMetrics.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        modifier = modifier
            .pressScale(interaction)
            .widthIn(min = metrics.buttonWidth, max = metrics.buttonWidth * 1.4f)
            .height(metrics.buttonHeight),
        shape = RoundedCornerShape(metrics.cardRadius),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (pressed) containerColor.copy(alpha = 0.82f) else containerColor,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF616161),
            disabledContentColor = Color.White.copy(alpha = 0.7f),
        ),
    ) {
        Text(text = label, style = metrics.label, maxLines = 1)
    }
}
