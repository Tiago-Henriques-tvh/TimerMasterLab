package com.tiagohenriques.project.timermasterlab.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Centralized screen-metrics for responsive sizing across the app.
 *
 * Use [LocalWindowSize] anywhere instead of manually threading screenWidth through params.
 */
@Immutable
data class WindowSize(
    val widthDp: Int,
    val heightDp: Int,
) {
    enum class WidthClass { Compact, Medium, Expanded }

    val widthClass: WidthClass
        get() =
            when {
                widthDp < 600 -> WidthClass.Compact
                widthDp < 840 -> WidthClass.Medium
                else -> WidthClass.Expanded
            }

    /**
     * Small scalar to make paddings/text a bit larger on larger screens.
     * Keep it conservative to avoid huge jumps.
     */
    val scale: Float
        get() =
            when (widthClass) {
                WidthClass.Compact -> 1.0f
                WidthClass.Medium -> 1.1f
                WidthClass.Expanded -> 1.25f
            }
}

val LocalWindowSize = staticCompositionLocalOf<WindowSize> {
    error("LocalWindowSize not provided. Wrap your UI in ProvideWindowSize().")
}

@Composable
fun rememberWindowSize(): WindowSize {
    val config = LocalConfiguration.current
    return WindowSize(widthDp = config.screenWidthDp, heightDp = config.screenHeightDp)
}
