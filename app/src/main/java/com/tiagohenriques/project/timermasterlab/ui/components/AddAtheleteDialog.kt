package com.tiagohenriques.project.timermasterlab.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens.DraftStartListItem
import com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens.StartListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAthleteDialog(
    athlete: Athlete? = null,
    onDismiss: () -> Unit,
    onConfirm: (DraftStartListItem) -> Unit,
    uiState: StartListUiState
) {
    var name by remember { mutableStateOf(athlete?.name ?: "") }
    var age by remember { mutableStateOf(athlete?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(athlete?.gender ?: "-") }
    var genderExpanded by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf(athlete?.category ?: 0) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("-", "M", "F")
    var distance by remember {
        mutableStateOf(
            uiState.draft.firstOrNull { it.athlete.id == athlete?.id }?.distanceMeters?.toString() ?: ""
        )
    }
    var startOrder by remember {
        mutableStateOf(
            uiState.draft.firstOrNull { it.athlete.id == athlete?.id }?.startOrder?.toString() ?: ""
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (athlete == null) "Add Athlete" else "Edit Athlete") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                OutlinedTextField(
                    value = age,
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            age = newValue
                        }
                    },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                ExposedDropdownMenuBox(
                    expanded = genderExpanded,
                    onExpandedChange = { genderExpanded = !genderExpanded }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genderExpanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = genderExpanded,
                        onDismissRequest = { genderExpanded = false }
                    ) {
                        genderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    gender = option
                                    genderExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = distance,
                    onValueChange = { if (it.matches(Regex("""\d*\.?\d*"""))) distance = it },
                    label = { Text("Distance (meters)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = Category.entries[category].displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                        modifier = Modifier.menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        Category.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName) },
                                onClick = {
                                    category = option.code
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                if (athlete != null) {
                    OutlinedTextField(
                        value = startOrder,
                        onValueChange = { if (it.all(Char::isDigit)) startOrder = it },
                        label = { Text("Start order") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val updatedAthlete = athlete?.copy(
                        name = name,
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        category = category
                    ) ?: Athlete(
                        name = name,
                        age = age.toIntOrNull() ?: 0,
                        gender = gender,
                        category = category
                    )

                    onConfirm(
                        DraftStartListItem(
                            athlete = updatedAthlete,
                            startOrder = startOrder.toIntOrNull() ?: (uiState.draft.size + 1),
                            distanceMeters = distance.toFloatOrNull() ?: 0f
                        )
                    )
                }
            ) {
                Text(if (athlete == null) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
