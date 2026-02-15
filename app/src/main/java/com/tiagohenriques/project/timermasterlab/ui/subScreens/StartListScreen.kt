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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
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

@Composable
fun StartListScreen(navController: NavHostController, mode: String = "Time Trial") {
    val viewModel: StartListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(mode) {
        viewModel.loadStartListForMode(mode)
    }

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
            modifier = Modifier
                .fillMaxSize(),
            uiState = uiState,
            onRemove = viewModel::removeDraftItem,
            onAdd = {
                viewModel.addAthleteToDraft(
                    athlete = it.athlete,
                    distanceMeters = it.distanceMeters,
                    startOrder = it.startOrder
                )
            },
            onEdit = {
                viewModel.updateDraftItem(it)
            },
            onExport = {
                viewModel.exportStartListToPDF(context)
            },
            save = { viewModel.commitStartList() },
            onMove = viewModel::moveDraftItem,
            mode = mode
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
    onMove: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    mode: String,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<DraftStartListItem?>(null) }

    Column(
        modifier = modifier,
    ) {
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

        Text(
            text = "Hold to drag and readjust athletes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = 12.dp)
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
                        key = { it.athlete.id }
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
                    // TODO: GROUP IN A BOX THE ITEMS/ATHLETES
                    // LazyColumn() { }
                }
            }
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
            },
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
        )
    }
}

@Composable
private fun AthleteCard(
    item: DraftStartListItem,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    isDragging: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
        onExport = {},
        save = {},
        mode = "Time Trial"
    )
}
