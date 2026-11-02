package com.tiagohenriques.project.timermasterlab.ui.responsive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun ProvideWindowSize(content: @Composable () -> Unit) {
    val windowSize = rememberWindowSize()
    CompositionLocalProvider(LocalWindowSize provides windowSize) {
        val metrics = rememberUiMetrics()
        CompositionLocalProvider(LocalUiMetrics provides metrics) {
            content()
        }
    }
}

