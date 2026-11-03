package com.tiagohenriques.project.timermasterlab.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import kotlinx.coroutines.launch

@Composable
fun AthleteManagementScreen(
    repository: AthletesRepository,
    modifier: Modifier = Modifier,
) {
    val athletes by repository.getAllAthletes().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editingAthlete by remember { mutableStateOf<Athlete?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var athleteToDelete by remember { mutableStateOf<Athlete?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(onClick = {
            editingAthlete = null
            showEditor = true
        }) {
            Text("Add athlete")
        }

        if (athletes.isEmpty()) {
            Text("No athletes yet. Add an athlete to get started.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(athletes, key = { it.id }) { athlete ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(athlete.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                                Text("${athlete.age} · ${athlete.gender} · ${Category.fromCode(athlete.category)}")
                            }
                            TextButton(onClick = {
                                editingAthlete = athlete
                                showEditor = true
                            }) {
                                Text("Edit")
                            }
                            TextButton(onClick = { athleteToDelete = athlete }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditor) {
        AthleteEditorDialog(
            athlete = editingAthlete,
            onDismiss = { showEditor = false },
            onSave = { athlete ->
                scope.launch {
                    if (athlete.id == 0L) repository.insert(athlete) else repository.update(athlete)
                }
                showEditor = false
            },
        )
    }

    athleteToDelete?.let { athlete ->
        AlertDialog(
            onDismissRequest = { athleteToDelete = null },
            title = { Text("Delete athlete?") },
            text = { Text("Delete ${athlete.name}? This also removes the athlete from any start lists.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { repository.delete(athlete) }
                    athleteToDelete = null
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { athleteToDelete = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
internal fun AthleteEditorDialog(
    athlete: Athlete?,
    onDismiss: () -> Unit,
    onSave: (Athlete) -> Unit,
) {
    var name by remember(athlete) { mutableStateOf(athlete?.name.orEmpty()) }
    var age by remember(athlete) { mutableStateOf(athlete?.age?.toString().orEmpty()) }
    var gender by remember(athlete) { mutableStateOf(athlete?.gender.orEmpty()) }
    var category by remember(athlete) {
        mutableStateOf(Category.entries.firstOrNull { it.code == athlete?.category } ?: Category.Minor)
    }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    val parsedAge = age.toIntOrNull()
    val canSave = name.isNotBlank() && parsedAge != null && parsedAge >= 0 && gender.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (athlete == null) "Add athlete" else "Edit athlete") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it.filter(Char::isDigit) },
                    label = { Text("Age") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("Gender") },
                    singleLine = true,
                )
                Column {
                    OutlinedButton(onClick = { categoryMenuExpanded = true }) {
                        Text("Category: ${category.displayName}")
                    }
                    DropdownMenu(
                        expanded = categoryMenuExpanded,
                        onDismissRequest = { categoryMenuExpanded = false },
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    category = option
                                    categoryMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = {
                    onSave(
                        Athlete(
                            id = athlete?.id ?: 0L,
                            name = name.trim(),
                            age = parsedAge ?: 0,
                            gender = gender.trim(),
                            category = category.code,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
