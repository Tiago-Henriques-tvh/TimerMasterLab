package com.tiagohenriques.project.timermasterlab.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val lightColors = lightColorScheme(
    primary = Color(0xFF002949),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF113F67),
    onPrimaryContainer = Color(0xFF84ABD9),
    secondary = Color(0xFF04658B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF58A0C8),
    onSecondaryContainer = Color(0xFF00344A),
    tertiary = Color(0xFF635E4B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE5DDC5),
    onTertiaryContainer = Color(0xFF66614E),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = Color(0xFFF9F9FD),
    onBackground = Color(0xFF1A1C1F),
    surface = Color(0xFFF9F9FD),
    onSurface = Color(0xFF1A1C1F),
    surfaceVariant = Color(0xFFDFE2EC),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF73777F),
    outlineVariant = Color(0xFFC2C7CF),
)

private val darkColors = darkColorScheme(
    primary = Color(0xFFA3CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF113F67),
    onPrimaryContainer = Color(0xFF84ABD9),
    secondary = Color(0xFF88CFF9),
    onSecondary = Color(0xFF00344A),
    secondaryContainer = Color(0xFF58A0C8),
    onSecondaryContainer = Color(0xFF00344A),
    tertiary = Color(0xFFFFF9EF),
    onTertiary = Color(0xFF343020),
    tertiaryContainer = Color(0xFFE5DDC5),
    onTertiaryContainer = Color(0xFF66614E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF121316),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF121316),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CF),
    outline = Color(0xFF8C9199),
    outlineVariant = Color(0xFF42474E),
)

@Composable
fun TimerMasterLabTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkColors else lightColors,
        content = content,
    )
}
