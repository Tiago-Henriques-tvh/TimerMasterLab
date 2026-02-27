package com.tiagohenriques.project.timermasterlab.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

@Composable
fun SettingsScreen(
    changeTheme: (Boolean) -> Unit,
    isBlackTheme: Boolean = false,
) {
    val localContext = LocalContext.current

    LazyColumn(
        modifier = Modifier.padding(vertical = 16.dp),
    ) {
        item { SettingsSectionHeader(title = "Appearance") }
        item {
            SwitchSettingItem(
                title = "Dark Mode",
                description = "Enable a darker theme for the app",
                icon = Icons.Outlined.DarkMode,
                isChecked = isBlackTheme,
                onToggle = changeTheme,
            )
        }
        item { SettingsSectionHeader(title = "Legal") }
        item {
            SimpleSettingItem(
                title = "Privacy Policy",
                description = "Read our privacy policy",
                icon = Icons.Outlined.PrivacyTip,
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.termsfeed.com/live/6afbc378-f1fa-4b51-8efc-f33599b2c992")
                    )
                    localContext.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun SwitchSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(text = title, fontWeight = FontWeight.Normal)
        },
        supportingContent = {
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = isChecked,
                onCheckedChange = null,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = { onToggle(!isChecked) })
                .padding(horizontal = 16.dp),
    )
}

@Composable
fun SimpleSettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(text = title)
        },
        supportingContent = {
            Text(text = description, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
    )
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
