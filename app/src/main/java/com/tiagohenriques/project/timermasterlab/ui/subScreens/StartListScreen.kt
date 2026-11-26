package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.tiagohenriques.project.timermasterlab.ui.responsive.LocalUiMetrics
import com.tiagohenriques.project.timermasterlab.ui.components.AddAthleteDialog
import com.tiagohenriques.project.timermasterlab.ui.components.ReorderableLazyColumn
import com.tiagohenriques.project.timermasterlab.ui.utils.shareStartListFile
import kotlinx.coroutines.launch

private data class StartListToolbarActions(
    val onSave: () -> Unit,
    val onBack: () -> Unit,
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
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val requestBack = {
        if (!isSaving) {
            if (uiState.hasUnsavedChanges) {
                showDiscardConfirm = true
            } else {
                navController.navigateUp()
            }
        }
    }

    BackHandler(onBack = requestBack)

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
            if (!isSaving) {
                if (!uiState.hasUnsavedChanges) {
                    navController.navigateUp()
                } else {
                    isSaving = true
                    coroutineScope.launch {
                        try {
                            viewModel.commitStartList()
                            isSaving = false
                            navController.navigateUp()
                        } catch (error: Exception) {
                            isSaving = false
                            Toast.makeText(
                                context,
                                "Could not save start list: ${error.message ?: "Please try again."}",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    }
                }
            }
        },
        onBack = requestBack,
        onAdd = { showAddDialog = true },
        onExport = {
            viewModel.exportStartListToPDF(context)
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
                isSaving = isSaving,
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

            if (showDiscardConfirm) {
                AlertDialog(
                    onDismissRequest = { showDiscardConfirm = false },
                    title = { Text("Discard unsaved changes?") },
                    text = {
                        Text("Your start-list edits will be lost if you leave without saving.")
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDiscardConfirm = false
                                navController.navigateUp()
                            },
                        ) {
                            Text("Discard")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDiscardConfirm = false }) {
                            Text("Keep editing")
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
    isSaving: Boolean,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    onRemove: (Long) -> Unit,
    onAddDraftItem: (DraftStartListItem) -> Unit,
    onEditDraftItem: (DraftStartListItem) -> Unit,
    onMoveDraftItem: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
) {
    var itemToEdit by remember { mutableStateOf<DraftStartListItem?>(null) }
    var isActionsMenuExpanded by remember { mutableStateOf(false) }
    val metrics = LocalUiMetrics.current

    val currentType = Type.fromStringToType(mode)
    val headerTitle = if (currentType == Type.GROUP_RACE) "Group Race" else "Time Trial"
    val headerIcon = if (currentType == Type.GROUP_RACE) Icons.Default.Groups else Icons.Default.Flag
    val subtitle = if (currentType == Type.GROUP_RACE) {
        "Athletes with the same group number start together."
    } else {
        "Drag athletes to set their start order."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = headerIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(metrics.icon),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerTitle,
                        style = metrics.title,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ListAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "${uiState.draft.size} ${
                                if (uiState.draft.size == 1) "athlete" else "athletes"
                            }",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = toolbarActions.onAdd,
                    enabled = !uiState.isLoading && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(metrics.buttonHeight),
                    shape = RoundedCornerShape(metrics.cardRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add athlete")
                }

                Box {
                    IconButton(
                        onClick = { isActionsMenuExpanded = true },
                        enabled = !uiState.isLoading && !isSaving,
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More start-list actions",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    DropdownMenu(
                        expanded = isActionsMenuExpanded,
                        onDismissRequest = { isActionsMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Import from ${toolbarActions.importLabel}") },
                            leadingIcon = { Icon(Icons.Default.GetApp, contentDescription = null) },
                            onClick = {
                                isActionsMenuExpanded = false
                                toolbarActions.onImportFromOtherMode()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Import JSON") },
                            leadingIcon = { Icon(Icons.Default.GetApp, contentDescription = null) },
                            onClick = {
                                isActionsMenuExpanded = false
                                toolbarActions.onImportJson()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Export PDF") },
                            leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                            enabled = uiState.draft.isNotEmpty(),
                            onClick = {
                                isActionsMenuExpanded = false
                                toolbarActions.onExport()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Share JSON") },
                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                            enabled = uiState.draft.isNotEmpty(),
                            onClick = {
                                isActionsMenuExpanded = false
                                toolbarActions.onShareJson()
                            },
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.draft.isEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = headerIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp),
                        )
                        Text(
                            text = "Your start list is empty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Add athletes to prepare this event.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else if (currentType == Type.TIME_TRIAL) {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                ) {
                    grouped.forEach { (startOrder, athletesInGroup) ->
                        item(key = "group_$startOrder") {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
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
                                            onClick = {},
                                            label = {
                                                Text(
                                                    "${athletesInGroup.size} ${
                                                        if (athletesInGroup.size == 1) "athlete" else "athletes"
                                                    }",
                                                )
                                            },
                                            colors = AssistChipDefaults.assistChipColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                                                labelColor = MaterialTheme.colorScheme.onSurface,
                                            ),
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (uiState.hasUnsavedChanges) {
                        "Unsaved changes"
                    } else {
                        "All changes saved"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (uiState.hasUnsavedChanges) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    },
                )
                Spacer(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(
                    onClick = toolbarActions.onBack,
                    enabled = !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(metrics.buttonHeight),
                    shape = RoundedCornerShape(metrics.cardRadius),
                ) {
                    Text("Back")
                }
                Button(
                    onClick = toolbarActions.onSave,
                    enabled = !uiState.isLoading && !isSaving,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(metrics.buttonHeight),
                    shape = RoundedCornerShape(metrics.cardRadius),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = when {
                            isSaving -> "Saving..."
                            uiState.hasUnsavedChanges -> "Save & return"
                            else -> "Done"
                        },
                        maxLines = 1,
                    )
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
    val distance = item.distanceMeters.toString().removeSuffix(".0")
    val demographics = buildList {
        if (item.athlete.age > 0) add("${item.athlete.age} yrs")
        when (item.athlete.gender) {
            "M" -> add("Male")
            "F" -> add("Female")
        }
    }
    val details = listOf(
        "$distance m",
        Category.fromCode(item.athlete.category),
    ) + demographics

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
                    text = details.joinToString(" · "),
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
            onBack = {},
            onAdd = {},
            onExport = {},
            onImportFromOtherMode = {},
            onImportJson = {},
            onShareJson = {},
            importLabel = "Import from Other Mode",
        ),
        isSaving = false,
        showAddDialog = false,
        onDismissAddDialog = {},
        onRemove = {},
        onAddDraftItem = {},
        onEditDraftItem = {},
        onMoveDraftItem = { _, _ -> },
    )
}
