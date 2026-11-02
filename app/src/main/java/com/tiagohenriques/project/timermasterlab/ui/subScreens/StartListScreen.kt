package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.di.AppViewModelProvider
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.components.AddAthleteDialog
import com.tiagohenriques.project.timermasterlab.ui.components.ReorderableLazyColumn
import com.tiagohenriques.project.timermasterlab.ui.utils.shareStartListFile

private data class StartListToolbarActions(
    val onSave: () -> Unit,
    val onAdd: () -> Unit,
    val onExport: () -> Unit,
    val onImportFromOtherMode: () -> Unit,
    val onImportJson: () -> Unit,
    val onShareJson: () -> Unit,
    val importLabel: String,
)

@Composable
fun StartListScreen(
    navController: NavHostController,
    mode: String = "Time Trial",
) {
    val viewModel: StartListViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showImportConfirm by remember { mutableStateOf(false) }
    var showJsonImportConfirm by remember { mutableStateOf(false) }
    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.importStartListFromJson(context, uri)
        }
    }

    LaunchedEffect(mode) {
        viewModel.loadStartListForMode(mode)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StartListEvent.ShareFile -> shareStartListFile(context, event.file)
                is StartListEvent.Message -> {
                    Toast.makeText(context, event.text, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val currentType = Type.fromStringToType(mode)
    val otherType = if (currentType == Type.GROUP_RACE) Type.TIME_TRIAL else Type.GROUP_RACE
    val importLabel = if (otherType == Type.TIME_TRIAL) "Time Trial" else "Group Race"

    val toolbarActions = StartListToolbarActions(
        onSave = {
            viewModel.commitStartList()
            navController.navigateUp()
        },
        onAdd = { showAddDialog = true },
        onExport = {
            viewModel.exportStartListToPDF(context)
            viewModel.commitStartList()
        },
        onImportFromOtherMode = { showImportConfirm = true },
        onImportJson = { showJsonImportConfirm = true },
        onShareJson = { viewModel.shareStartListJson(context) },
        importLabel = importLabel,
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { _ ->
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            StartListContent(
                modifier = Modifier.fillMaxSize(),
                uiState = uiState,
                mode = mode,
                toolbarActions = toolbarActions,
                showAddDialog = showAddDialog,
                onDismissAddDialog = { showAddDialog = false },
                onRemove = viewModel::removeDraftItem,
                onAddDraftItem = { draft ->
                    viewModel.addAthleteToDraft(
                        athlete = draft.athlete,
                        distanceMeters = draft.distanceMeters,
                        startOrder = draft.startOrder,
                    )
                },
                onEditDraftItem = viewModel::updateDraftItem,
                onMoveDraftItem = viewModel::moveDraftItem,
            )

            if (showImportConfirm) {
                AlertDialog(
                    onDismissRequest = { showImportConfirm = false },
                    title = { Text(toolbarActions.importLabel) },
                    text = {
                        Text(
                            "This will remove all athletes in the current start list and replace them with the athletes from the other mode.\n\nDo you want to continue?"
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                viewModel.importStartListFrom(otherType)
                                showImportConfirm = false
                            }
                        ) {
                            Text("Replace")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showImportConfirm = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            if (showJsonImportConfirm) {
                AlertDialog(
                    onDismissRequest = { showJsonImportConfirm = false },
                    title = { Text("Import JSON start list") },
                    text = {
                        Text(
                            "This replaces the current start list with athletes from a JSON file. Save afterwards to keep it.",
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showJsonImportConfirm = false
                                importJsonLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                            },
                        ) {
                            Text("Choose file")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showJsonImportConfirm = false }) {
                            Text("Cancel")
                        }
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
    mode: String,
    toolbarActions: StartListToolbarActions,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onRemove: (Long) -> Unit,
    onAddDraftItem: (DraftStartListItem) -> Unit,
    onEditDraftItem: (DraftStartListItem) -> Unit,
    onMoveDraftItem: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
) {
    var itemToEdit by remember { mutableStateOf<DraftStartListItem?>(null) }
    val metrics = LocalUiMetrics.current

    val currentType = Type.fromStringToType(mode)
    val headerTitle = if (currentType == Type.GROUP_RACE) "Group Race" else "Time Trial"
    val headerIcon = if (currentType == Type.GROUP_RACE) Icons.Default.Groups else Icons.Default.Flag

    Column(
        modifier = modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = headerTitle,
                            style = metrics.title,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    AssistChip(
                        onClick = { /* informational */ },
                        label = { Text("${uiState.draft.size}") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    )
                }

                val actionShape = RoundedCornerShape(metrics.cardRadius)
                val actionHeight = metrics.buttonHeight
                val actionIconSize = metrics.icon * 0.8f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = toolbarActions.onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF008000),
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Save/Back")
                    }

                    FilledTonalButton(
                        onClick = toolbarActions.onImportFromOtherMode,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.GetApp,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(toolbarActions.importLabel, maxLines = 1)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalButton(
                        onClick = toolbarActions.onAdd,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Add")
                    }

                    FilledTonalButton(
                        onClick = toolbarActions.onExport,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading && uiState.draft.isNotEmpty(),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Export PDF")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    FilledTonalButton(
                        onClick = toolbarActions.onImportJson,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading,
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.GetApp,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Import JSON", maxLines = 1)
                    }

                    FilledTonalButton(
                        onClick = toolbarActions.onShareJson,
                        modifier = Modifier
                            .weight(1f)
                            .height(actionHeight),
                        enabled = !uiState.isLoading && uiState.draft.isNotEmpty(),
                        shape = actionShape,
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(actionIconSize),
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Share JSON", maxLines = 1)
                    }
                }

                if (currentType == Type.TIME_TRIAL) {
                    Text(
                        text = "Tip: hold and drag to reorder athletes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                if (currentType == Type.TIME_TRIAL) {
                    ReorderableLazyColumn(
                        items = uiState.draft,
                        modifier = Modifier.fillMaxSize(),
                        onMove = { from, to ->
                            Log.d("StartList", "onMove called: from=$from to=$to")
                            onMoveDraftItem(from, to)
                        },
                        key = { item -> "${item.athlete.id}_${item.startOrder}" },
                    ) { item, isDragging, dragModifier ->
                        AthleteCard(
                            item = item,
                            isDragging = isDragging,
                            modifier = dragModifier,
                            onEdit = { itemToEdit = item },
                            onRemove = { onRemove(item.athlete.id) },
                        )
                    }
                } else {
                    val grouped = uiState.draft
                        .sortedBy { it.startOrder }
                        .groupBy { it.startOrder }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                    ) {
                        grouped.forEach { (startOrder, athletesInGroup) ->
                            item(key = "group_$startOrder") {
                                Card(
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                text = "Start Group $startOrder",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.weight(1f),
                                            )

                                            AssistChip(
                                                onClick = { /* info-only */ },
                                                label = { Text("${athletesInGroup.size}") },
                                                colors = AssistChipDefaults.assistChipColors(
                                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                    labelColor = MaterialTheme.colorScheme.onSurface,
                                                )
                                            )
                                        }

                                        athletesInGroup.forEach { item ->
                                            AthleteCard(
                                                item = item,
                                                onEdit = { itemToEdit = item },
                                                onRemove = { onRemove(item.athlete.id) },
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
                onAddDraftItem(it)
                onDismissAddDialog()
            },
            isTimeTrial = currentType == Type.TIME_TRIAL,
        )
    }

    itemToEdit?.let { item ->
        AddAthleteDialog(
            athlete = item.athlete,
            uiState = uiState,
            onDismiss = { itemToEdit = null },
            onConfirm = {
                onEditDraftItem(it)
                itemToEdit = null
            },
            isTimeTrial = currentType == Type.TIME_TRIAL,
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
            },
            contentColor = if (isDragging) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        ),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.startOrder}. ${item.athlete.name}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "${item.distanceMeters}m • ${Category.fromCode(item.athlete.category)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    Icon(Icons.Default.Draw, contentDescription = "Edit athlete")
                }

                IconButton(
                    onClick = onRemove,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove athlete")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartListScreenPreview() {
    StartListContent(
        uiState = StartListUiState(),
        mode = "Time Trial",
        toolbarActions = StartListToolbarActions(
            onSave = {},
            onAdd = {},
            onExport = {},
            onImportFromOtherMode = {},
            onImportJson = {},
            onShareJson = {},
            importLabel = "Import from Other Mode",
        ),
        showAddDialog = false,
        onDismissAddDialog = {},
        onRemove = {},
        onAddDraftItem = {},
        onEditDraftItem = {},
        onMoveDraftItem = { _, _ -> },
    )
}
