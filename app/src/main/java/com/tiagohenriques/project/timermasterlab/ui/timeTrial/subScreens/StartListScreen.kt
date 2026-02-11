package com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.components.ReorderableLazyColumn

@Composable
fun StartListScreen(navController: NavHostController) {
    val viewModel: StartListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.commitStartList()
                    navController.navigateUp()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Commit Start List",
                )
            }
        },
    ) { _ ->
        StartListContent(
            modifier = Modifier.fillMaxSize(),
            uiState = uiState,
            onRemove = viewModel::removeDraftItem,
            onAdd = {
                viewModel.addAthleteToDraft(
                    athlete = it.athlete,
                    distanceMeters = it.distanceMeters
                )
            },
            onEdit = {
                viewModel.updateDraftItem(it)
            },
            onExport = {
                viewModel.exportStartListToPDF(context)
            },
            save = { viewModel.commitStartList() },
            onMove = viewModel::moveDraftItem
        )
    }
}

@Composable
private fun StartListContent(
    modifier: Modifier = Modifier,
    uiState: StartListUiState,
    onRemove: (Long) -> Unit,
    onAdd: (DraftStartListItem) -> Unit,
    onEdit: (DraftStartListItem) -> Unit,
    onExport: () -> Unit,
    save: () -> Unit,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> }
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<DraftStartListItem?>(null) }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Athlete")
                Spacer(Modifier.width(8.dp))
                Text("Add Athlete")
            }

            FilledTonalButton(
                onClick = {
                    onExport()
                    save()
                },
                modifier = Modifier.width(120.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export")
                Spacer(Modifier.width(4.dp))
                Text("Export")
            }
        }

        ReorderableLazyColumn(
            items = uiState.draft,
            modifier = Modifier.padding(PaddingValues(12.dp)),
            onMove = { from, to ->
                Log.d("StartList", "onMove called: from=$from to=$to")
                onMove(from, to)
            },
            key = { it.athlete.id }
        ) { item, isDragging, dragModifier ->
            AthleteCard(
                item = item,
                isDragging = isDragging,
                dragModifier = dragModifier,
                onEdit = { itemToEdit = item  },
                onRemove = { onRemove(item.athlete.id) }
            )
        }
    }

    if (showAddDialog) {
        AddAthleteDialog(
            athlete = null,
            uiState = uiState,
            onDismiss = { showAddDialog = false },
            onConfirm = {
                onAdd(it)
                showAddDialog = false
            }
        )
    }

    itemToEdit?.let { item ->
        AddAthleteDialog(
            athlete = item.athlete,
            uiState = uiState,
            onDismiss = { itemToEdit = null },
            onConfirm = {
                onEdit(it)
                itemToEdit = null
            }
        )
    }
}

@Composable
private fun AthleteCard(
    item: DraftStartListItem,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    isDragging: Boolean = false,
    dragModifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = dragModifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.startOrder}. ${item.athlete.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${item.distanceMeters}m • ${Category.fromCode(item.athlete.category)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Draw, contentDescription = "Edit athlete")
                }

                IconButton(
                    onClick = onRemove,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove athlete")
                }
            }
        }
    }
}

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

@Preview(showBackground = true)
@Composable
fun StartListScreenPreview() {
    StartListContent(uiState = StartListUiState(), onRemove = {}, onAdd = {}, onEdit = {}, onExport = {}, save = {})
}
