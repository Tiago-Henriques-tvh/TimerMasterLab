package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.ui.subScreens.DraftStartListItem
import com.tiagohenriques.project.timermasterlab.ui.subScreens.StartListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAthleteDialog(
    athlete: Athlete? = null,
    onDismiss: () -> Unit,
    onConfirm: (DraftStartListItem) -> Unit,
    uiState: StartListUiState,
    isTimeTrial: Boolean,
) {
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf(athlete?.name ?: "") }
    var age by remember { mutableStateOf(athlete?.age?.toString() ?: "") }

    var gender by remember { mutableStateOf(athlete?.gender ?: "-") }
    var genderExpanded by remember { mutableStateOf(false) }
    val genderOptions = remember { listOf("-", "M", "F") }

    // Keep previous behavior but guard against invalid indexes
    var category by remember { mutableStateOf(athlete?.category ?: 0) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val existingDraft = remember(uiState.draft, athlete?.id) {
        uiState.draft.firstOrNull { it.athlete.id == athlete?.id }
    }

    var distance by remember { mutableStateOf(existingDraft?.distanceMeters?.toString() ?: "") }
    var startOrder by remember { mutableStateOf(existingDraft?.startOrder?.toString() ?: "") }

    val isEditing = athlete != null
    val titleText = if (isEditing) "Edit Athlete" else "Add Athlete"

    val categoryEntry = Category.entries.firstOrNull { it.code == category } ?: Category.entries.first()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) age = newValue
                    },
                    label = { Text("Age") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded },
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false },
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = distance,
                    onValueChange = { input ->
                        if (input.all(Char::isDigit)) {
                            distance = input
                        }
                    },
                    label = { Text("Distance (m)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = if (isEditing || !isTimeTrial) ImeAction.Next else ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded },
                ) {
                    OutlinedTextField(
                        value = categoryEntry.displayName,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    category = option.code
                                    categoryExpanded = false
                                },
                            )
                        }
                    }
                }

                if (isEditing || !isTimeTrial) {
                    OutlinedTextField(
                        value = startOrder,
                        onValueChange = { input ->
                            if (input.all(Char::isDigit)) startOrder = input
                        },
                        label = { Text("Start order") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done,
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    focusManager.clearFocus()

                    val updatedAthlete = athlete?.copy(
                        name = name.trim(),
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        category = category,
                    ) ?: Athlete(
                        name = name.trim(),
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        category = category,
                    )

                    onConfirm(
                        DraftStartListItem(
                            athlete = updatedAthlete,
                            startOrder = startOrder.toIntOrNull() ?: (uiState.draft.size + 1),
                            distanceMeters = distance.toFloatOrNull() ?: 0f,
                        ),
                    )
                },
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    focusManager.clearFocus()
                    onDismiss()
                },
                colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            ) {
                Text("Cancel")
            }
        },
    )
}
