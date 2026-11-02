package com.tiagohenriques.project.timermasterlab.ui.responsive

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min

/**
 * Sizes derived from the shorter screen edge so controls stay proportional
 * on phones and tablets instead of jumping to fixed oversized values.
 */
@Immutable
data class UiMetrics(
    val scale: Float,
    val screenPadding: Dp,
    val gap: Dp,
    val buttonHeight: Dp,
    val buttonWidth: Dp,
    val icon: Dp,
    val logo: Dp,
    val cardRadius: Dp,
    val homeCardHeight: Dp,
    val headerHeight: Dp,
    val menuRowHeight: Dp,
    val title: TextStyle,
    val body: TextStyle,
    val label: TextStyle,
    val clock: TextStyle,
)

val LocalUiMetrics = staticCompositionLocalOf { phoneMetrics() }

@Composable
fun rememberUiMetrics(): UiMetrics {
    val window = LocalWindowSize.current
    val shortest = min(window.widthDp, window.heightDp).coerceIn(300, 720)
    val scale = (shortest / 400f).coerceIn(0.86f, 1.08f)
    val type = MaterialTheme.typography
    return UiMetrics(
        scale = scale,
        screenPadding = (12f * scale).dp,
        gap = (8f * scale).dp,
        buttonHeight = (40f * scale).dp,
        buttonWidth = (84f * scale).dp,
        icon = (22f * scale).dp,
        logo = (32f * scale).dp,
        cardRadius = (14f * scale).dp,
        homeCardHeight = (76f * scale).dp,
        headerHeight = (132f * scale).dp,
        menuRowHeight = (68f * scale).dp,
        title = type.titleMedium.copy(
            fontSize = (16f * scale).sp,
            fontWeight = FontWeight.SemiBold,
        ),
        body = type.bodyMedium.copy(fontSize = (13f * scale).sp),
        label = type.labelLarge.copy(
            fontSize = (13f * scale).sp,
            fontWeight = FontWeight.Bold,
        ),
        clock = type.titleLarge.copy(
            fontSize = (22f * scale).sp,
            fontWeight = FontWeight.Bold,
        ),
    )
}

private fun phoneMetrics(): UiMetrics {
    val title = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    return UiMetrics(
        scale = 1f,
        screenPadding = 12.dp,
        gap = 8.dp,
        buttonHeight = 40.dp,
        buttonWidth = 84.dp,
        icon = 22.dp,
        logo = 32.dp,
        cardRadius = 14.dp,
        homeCardHeight = 76.dp,
        headerHeight = 132.dp,
        menuRowHeight = 68.dp,
        title = title,
        body = TextStyle(fontSize = 13.sp),
        label = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold),
        clock = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    )
}
