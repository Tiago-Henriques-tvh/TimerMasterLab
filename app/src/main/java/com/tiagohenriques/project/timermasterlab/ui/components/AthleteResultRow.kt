package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Shared row used by both Time Trial and Group Race live screens.
 *
 * It renders the common layout (start order bubble + athlete name + duration) and lets callers
 * inject a trailing control (START/STOP/✓ etc.).
 */
@Composable
fun AthleteResultRow(
    startOrder: Int,
    athleteName: String,
    durationText: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = startOrder.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(Modifier.width(4.dp))

            Text(
                text = athleteName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = durationText,
                modifier = Modifier.width(72.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End,
            )

            Spacer(Modifier.width(4.dp))

            Box(
                modifier = Modifier.width(80.dp),
                contentAlignment = Alignment.Center,
            ) {
                trailing()
            }
        }
    }
}

@Composable
fun FinishedIcon(
    modifier: Modifier = Modifier,
    tint: Color = Color(0xFF2E7D32),
) {
    Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = tint,
        modifier = modifier.size(28.dp),
    )
}
