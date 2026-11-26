package com.tiagohenriques.project.timermasterlab.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

@Composable
fun SettingsScreen(
    changeTheme: (Boolean) -> Unit,
    isBlackTheme: Boolean = false,
    fileLoggingEnabled: Boolean = true,
    onFileLoggingChange: (Boolean) -> Unit = {},
) {
    val localContext = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 20.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsGroup(
                    title = "Appearance",
                    description = "Choose how Timer Master Lab looks.",
                ) {
                    SwitchSettingItem(
                        title = "Dark mode",
                        description = "Use a darker palette in low light.",
                        icon = Icons.Outlined.DarkMode,
                        isChecked = isBlackTheme,
                        onToggle = changeTheme,
                    )
                }
            }
            item {
                SettingsGroup(
                    title = "Diagnostics",
                    description = "Help keep the app reliable.",
                ) {
                    SwitchSettingItem(
                        title = "File logging",
                        description = "Keep diagnostic logs to help investigate unexpected closes.",
                        icon = Icons.AutoMirrored.Outlined.Article,
                        isChecked = fileLoggingEnabled,
                        onToggle = onFileLoggingChange,
                    )
                }
            }
            item {
                SettingsGroup(
                    title = "Legal",
                    description = "Policies and important information.",
                ) {
                    SimpleSettingItem(
                        title = "Privacy policy",
                        description = "See how your information is handled.",
                        icon = Icons.Outlined.PrivacyTip,
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.termsfeed.com/live/6afbc378-f1fa-4b51-8efc-f33599b2c992"),
                            )
                            localContext.startActivity(intent)
                        },
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Created by",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Tiago Henriques",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    description: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) {
            content()
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = { SettingIcon(icon = icon) },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .toggleable(
                value = isChecked,
                role = Role.Switch,
                onValueChange = onToggle,
            ),
    )
}

@Composable
private fun SimpleSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.SemiBold)
        },
        supportingContent = {
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = { SettingIcon(icon = icon) },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
    )
}

@Composable
private fun SettingIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(13.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TimerMasterLabTheme {
        SettingsScreen(
            changeTheme = { _ -> },
        )
    }
}
