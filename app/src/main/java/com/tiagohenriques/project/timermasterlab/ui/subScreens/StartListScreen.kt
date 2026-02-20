package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.components.AddAthleteDialog
import com.tiagohenriques.project.timermasterlab.ui.components.ReorderableLazyColumn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding

@Composable
fun StartListScreen(navController: NavHostController, mode: String = "Time Trial") {
    val viewModel: StartListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(mode) {
        viewModel.loadStartListForMode(mode)
    }

    Scaffold(
        bottomBar = {
            StartListSplitActions(
                onSave = {
                    viewModel.commitStartList()
                    navController.navigateUp()
                },
                onAdd = { showAddDialog = true },
                onExport = {
                    viewModel.exportStartListToPDF(context)
                    viewModel.commitStartList()
                },
            )
        },
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            StartListContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                onRemove = viewModel::removeDraftItem,
                onAdd = {
                    viewModel.addAthleteToDraft(
                        athlete = it.athlete,
                        distanceMeters = it.distanceMeters,
                        startOrder = it.startOrder
                    )
                },
                onEdit = { viewModel.updateDraftItem(it) },
                onMove = viewModel::moveDraftItem,
                mode = mode,
                showAddDialog = showAddDialog,
                onDismissAddDialog = { showAddDialog = false },
            )
        }
    }
}

@Composable
private fun StartListSplitActions(
    onSave: () -> Unit,
    onAdd: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    BottomAppBar(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Button(
            onClick = onSave,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.size(8.dp))
            Text("Save")
        }

        Box(modifier = Modifier.padding(start = 8.dp, end = 12.dp)) {
            FilledTonalIconButton(
                onClick = { menuExpanded = true },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "More actions")
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Add athlete") },
                    leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onAdd()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Export") },
                    leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onExport()
                    },
                )
            }
        }
    }
}

@Composable
private fun StartListContent(
    modifier: Modifier = Modifier,
    uiState: StartListUiState,
    onRemove: (Long) -> Unit,
    onAdd: (DraftStartListItem) -> Unit,
    onEdit: (DraftStartListItem) -> Unit,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    mode: String,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
) {
    var itemToEdit by remember { mutableStateOf<DraftStartListItem?>(null) }

    Column(modifier = modifier) {
        Text(
            text = "Hold to drag and readjust athletes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (Type.fromStringToType(mode) == Type.TIME_TRIAL) {
                    ReorderableLazyColumn(
                        items = uiState.draft,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(PaddingValues(12.dp)),
                        onMove = { from, to ->
                            Log.d("StartList", "onMove called: from=$from to=$to")
                            onMove(from, to)
                        },
                        key = { item -> "${item.athlete.id}_${item.startOrder}" }
                    ) { item, isDragging, dragModifier ->
                        AthleteCard(
                            item = item,
                            isDragging = isDragging,
                            modifier = dragModifier,
                            onEdit = { itemToEdit = item },
                            onRemove = { onRemove(item.athlete.id) }
                        )
                    }
                } else {
                    val grouped = uiState.draft
                        .sortedBy { it.startOrder }
                        .groupBy { it.startOrder }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        grouped.forEach { (startOrder, athletesInGroup) ->

                            item(key = "group_$startOrder") {

                                Card(
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {

                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {

                                        Text(
                                            text = "Start Group $startOrder",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )

                                        athletesInGroup.forEach { item ->

                                            AthleteCard(
                                                item = item,
                                                onEdit = { itemToEdit = item },
                                                onRemove = { onRemove(item.athlete.id) }
                                            )

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAthleteDialog(
            athlete = null,
            uiState = uiState,
            onDismiss = onDismissAddDialog,
            onConfirm = {
                onAdd(it)
                onDismissAddDialog()
            },
            isTimeTrial = Type.fromStringToType(mode) == Type.TIME_TRIAL
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
            },
            isTimeTrial = Type.fromStringToType(mode) == Type.TIME_TRIAL
        )
    }
}

@Composable
private fun AthleteCard(
    item: DraftStartListItem,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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

@Preview(showBackground = true)
@Composable
fun StartListScreenPreview() {
    StartListContent(
        uiState = StartListUiState(),
        onRemove = {},
        onAdd = {},
        onEdit = {},
        mode = "Time Trial",
        modifier = Modifier,
        onMove = { _, _ -> },
        showAddDialog = false,
        onDismissAddDialog = {},
    )
}
