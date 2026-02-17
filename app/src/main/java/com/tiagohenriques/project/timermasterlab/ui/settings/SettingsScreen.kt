package com.tiagohenriques.project.timermasterlab.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.tiagohenriques.project.timermasterlab.ui.theme.TimerMasterLabTheme

@Composable
fun SettingsScreen(
    navController: NavHostController,
    changeTheme: (Boolean) -> Unit,
    isBlackTheme: Boolean = false,
    screenWidth: Int,
) {
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

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TimerMasterLabTheme {
        SettingsScreen(
            navController = rememberNavController(),
            changeTheme = { _ -> },
            screenWidth = 600
        )
    }
}
