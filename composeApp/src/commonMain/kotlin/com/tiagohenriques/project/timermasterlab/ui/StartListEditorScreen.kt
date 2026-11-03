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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tiagohenriques.project.timermasterlab.data.StartListDocument
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.StartListEntryRepository
import com.tiagohenriques.project.timermasterlab.platform.currentTimeMillis
import com.tiagohenriques.project.timermasterlab.platform.shareFile
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.write
import io.github.vinceglb.filekit.writeString
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFileSaverLauncher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun StartListEditorScreen(
    mode: Type,
    athletesRepository: AthletesRepository,
    startListRepository: StartListEntryRepository,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val athletes by athletesRepository.getAllAthletes().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var orderedIds by remember(mode) { mutableStateOf<List<Long>>(emptyList()) }
    var distances by remember(mode) { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var groupOrders by remember(mode) { mutableStateOf<Map<Long, String>>(emptyMap()) }
    var isLoaded by remember(mode) { mutableStateOf(false) }
    var isBusy by remember { mutableStateOf(false) }
    var hasUnsavedChanges by remember(mode) { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var athleteToEdit by remember { mutableStateOf<Athlete?>(null) }
    var showAddAthlete by remember { mutableStateOf(false) }
    var showCopyOtherModeConfirmation by remember { mutableStateOf(false) }
    var showJsonImportConfirmation by remember { mutableStateOf(false) }

    fun currentEntries(): List<StartListEntry> = orderedIds.mapIndexedNotNull { index, athleteId ->
        val distance = distances[athleteId]?.toFloatOrNull() ?: return@mapIndexedNotNull null
        val startOrder = if (mode == Type.GROUP_RACE) {
            groupOrders[athleteId]?.toIntOrNull()?.takeIf { it > 0 } ?: return@mapIndexedNotNull null
        } else {
            index + 1
        }
        StartListEntry(
            athleteId = athleteId,
            startOrder = startOrder,
            distanceMeters = distance,
            type = mode.displayName,
        )
    }

    suspend fun exportJson(): String {
        return StartListDocument.encode(
            mode = mode,
            entries = currentEntries(),
            athletes = athletesRepository.getAllAthletesOnce().associateBy { it.id },
            exportedAtEpochMillis = currentTimeMillis(),
        )
    }

    suspend fun importDocument(rawJson: String) {
        val document = StartListDocument.decode(rawJson)
        val knownAthletes = athletesRepository.getAllAthletesOnce().toMutableList()
        val usedIds = mutableSetOf<Long>()
        val importedEntries = document.entries.sortedBy { it.startOrder }.map { entry ->
            val matched = knownAthletes.firstOrNull { athlete ->
                athlete.id !in usedIds &&
                    athlete.name.equals(entry.name, ignoreCase = true) &&
                    athlete.age == entry.age &&
                    athlete.gender.equals(entry.gender, ignoreCase = true) &&
                    athlete.category == entry.category
            }
            val resolved = matched ?: Athlete(
                name = entry.name,
                age = entry.age,
                gender = entry.gender,
                category = entry.category,
            ).let { newAthlete ->
                newAthlete.copy(id = athletesRepository.insert(newAthlete))
            }.also(knownAthletes::add)
            usedIds += resolved.id
            Triple(resolved.id, entry.distanceMeters.toString(), entry.startOrder.toString())
        }
        orderedIds = importedEntries.map { it.first }
        distances = distances + importedEntries.associate { it.first to it.second }
        groupOrders = groupOrders + importedEntries.associate { it.first to it.third }
        hasUnsavedChanges = true
        feedback = "Imported ${importedEntries.size} athletes. Save to keep this start list."
    }

    val importLauncher = rememberFilePickerLauncher(
        type = FileKitType.File("json"),
        onResult = { file ->
            if (file != null) {
                scope.launch {
                    isBusy = true
                    try {
                        importDocument(file.readString())
                    } catch (exception: Exception) {
                        feedback = exception.message ?: "Could not import the selected file."
                    } finally {
                        isBusy = false
                    }
                }
            }
        },
    )
    val jsonSaver = rememberFileSaverLauncher { file ->
        if (file != null) {
            scope.launch {
                try {
                    file.writeString(exportJson())
                    feedback = "Start list exported."
                } catch (exception: Exception) {
                    feedback = exception.message ?: "Could not save the JSON file."
                }
            }
        }
    }
    val pdfSaver = rememberFileSaverLauncher { file ->
        if (file != null) {
            scope.launch {
                try {
                    file.write(
                        createStartListPdf(
                            currentEntries(),
                            athletesRepository.getAllAthletesOnce().associateBy { it.id },
                            "${mode.displayName} Start List",
                        ),
                    )
                    feedback = "PDF exported."
                } catch (exception: Exception) {
                    feedback = exception.message ?: "Could not save the PDF file."
                }
            }
        }
    }

    LaunchedEffect(mode) {
        try {
            val entries = when (mode) {
                Type.TIME_TRIAL -> startListRepository.getTimeTrialStartList().first()
                Type.GROUP_RACE -> startListRepository.getGroupRaceStartList().first()
            }
            orderedIds = entries.sortedBy { it.startOrder }.map { it.athleteId }
            distances = entries.associate { it.athleteId to it.distanceMeters.toString() }
            groupOrders = entries.associate { it.athleteId to it.startOrder.toString() }
            isLoaded = true
        } catch (exception: Exception) {
            feedback = exception.message ?: "Could not load the saved start list."
            isLoaded = true
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("${mode.displayName} start list", style = MaterialTheme.typography.headlineSmall)
                    Text("${orderedIds.size}", style = MaterialTheme.typography.titleMedium)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy,
                        onClick = {
                            if (!hasUnsavedChanges) {
                                onSaved()
                                return@Button
                            }
                            isBusy = true
                            scope.launch {
                                try {
                                    startListRepository.replaceStartList(mode, currentEntries())
                                    hasUnsavedChanges = false
                                    feedback = "Saved start list."
                                    onSaved()
                                } catch (exception: Exception) {
                                    feedback = exception.message ?: "Could not save the start list."
                                } finally {
                                    isBusy = false
                                }
                            }
                        },
                    ) {
                        Text(if (isBusy) "Working…" else "Save/Back")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy,
                        onClick = { showCopyOtherModeConfirmation = true },
                    ) {
                        Text("Copy other mode", maxLines = 1)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy,
                        onClick = {
                            athleteToEdit = null
                            showAddAthlete = true
                        },
                    ) {
                        Text("Add athlete")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy,
                        onClick = { showJsonImportConfirmation = true },
                    ) {
                        Text("Import JSON")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy,
                        onClick = {
                            jsonSaver.launch(
                                suggestedName = "startlist_${mode.name.lowercase()}",
                                extension = "json",
                            )
                        },
                    ) {
                        Text("Export JSON")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy && orderedIds.isNotEmpty(),
                        onClick = {
                            scope.launch {
                                try {
                                    shareFile(
                                        name = "startlist_${mode.name.lowercase()}.json",
                                        bytes = exportJson().encodeToByteArray(),
                                        mimeType = "application/json",
                                    )
                                } catch (exception: Exception) {
                                    feedback = exception.message ?: "Could not share the JSON file."
                                }
                            }
                        },
                    ) {
                        Text("Share JSON")
                    }
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        enabled = isLoaded && !isBusy && orderedIds.isNotEmpty(),
                        onClick = {
                            pdfSaver.launch(
                                suggestedName = "startlist_${mode.name.lowercase()}",
                                extension = "pdf",
                            )
                        },
                    ) {
                        Text("Export PDF")
                    }
                }
            }
        }

        if (!isLoaded) {
            Text("Loading start list…")
        } else if (athletes.isEmpty()) {
            Text("Add athletes before creating a start list.")
        } else {
            val selected = orderedIds.toSet()
            val orderedAthletes = if (mode == Type.GROUP_RACE) {
                athletes.sortedWith(
                    compareBy<Athlete> {
                        if (it.id in selected) groupOrders[it.id]?.toIntOrNull() ?: Int.MAX_VALUE
                        else Int.MAX_VALUE
                    }.thenBy {
                        orderedIds.indexOf(it.id).let { index -> if (index < 0) Int.MAX_VALUE else index }
                    }.thenBy { it.name.lowercase() },
                )
            } else {
                athletes.sortedWith(
                    compareBy<Athlete> {
                        orderedIds.indexOf(it.id).let { index -> if (index < 0) Int.MAX_VALUE else index }
                    }.thenBy { it.name.lowercase() },
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(orderedAthletes, key = { it.id }) { athlete ->
                    val orderIndex = orderedIds.indexOf(athlete.id)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Checkbox(
                                checked = athlete.id in selected,
                                onCheckedChange = { checked ->
                                    orderedIds = if (checked) orderedIds + athlete.id else orderedIds - athlete.id
                                    if (checked && mode == Type.GROUP_RACE) {
                                        val nextGroup =
                                            (groupOrders.values.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: 0) + 1
                                        groupOrders = groupOrders + (athlete.id to nextGroup.toString())
                                    }
                                    distances = if (checked && athlete.id !in distances) {
                                        distances + (athlete.id to "0")
                                    } else {
                                        distances
                                    }
                                    hasUnsavedChanges = true
                                    feedback = null
                                },
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when {
                                        orderIndex < 0 -> athlete.name
                                        mode == Type.GROUP_RACE ->
                                            "Group ${groupOrders[athlete.id].orEmpty()} · ${athlete.name}"
                                        else -> "${orderIndex + 1}. ${athlete.name}"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text("${athlete.age} · ${athlete.gender}")
                            }
                            if (orderIndex >= 0) {
                                TextButton(
                                    onClick = { athleteToEdit = athlete },
                                ) {
                                    Text("Edit")
                                }
                                if (mode == Type.TIME_TRIAL) {
                                    OutlinedButton(
                                        enabled = orderIndex > 0,
                                        onClick = {
                                            val reordered = orderedIds.toMutableList()
                                            reordered.add(orderIndex - 1, reordered.removeAt(orderIndex))
                                            orderedIds = reordered
                                            hasUnsavedChanges = true
                                        },
                                    ) { Text("↑") }
                                    OutlinedButton(
                                        enabled = orderIndex < orderedIds.lastIndex,
                                        onClick = {
                                            val reordered = orderedIds.toMutableList()
                                            reordered.add(orderIndex + 1, reordered.removeAt(orderIndex))
                                            orderedIds = reordered
                                            hasUnsavedChanges = true
                                        },
                                    ) { Text("↓") }
                                } else {
                                    OutlinedTextField(
                                        value = groupOrders[athlete.id].orEmpty(),
                                        onValueChange = { value ->
                                            groupOrders = groupOrders + (athlete.id to value.filter { it.isDigit() })
                                            hasUnsavedChanges = true
                                            feedback = null
                                        },
                                        label = { Text("Group") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.7f),
                                    )
                                }
                                OutlinedTextField(
                                    value = distances[athlete.id].orEmpty(),
                                    onValueChange = { value ->
                                        distances = distances + (athlete.id to value.filter {
                                            it.isDigit() || it == '.' || it == ','
                                        }.replace(',', '.'))
                                        hasUnsavedChanges = true
                                        feedback = null
                                    },
                                    label = { Text("Meters") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.7f),
                                )
                            }
                        }
                    }
                }
            }
        }

        feedback?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Saved") || message.startsWith("Imported") ||
                    message.startsWith("Copied") || message.endsWith("exported.")
                ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }

    if (showCopyOtherModeConfirmation) {
        AlertDialog(
            onDismissRequest = { showCopyOtherModeConfirmation = false },
            title = { Text("Copy other mode") },
            text = {
                Text("This replaces the current start list with the other mode's entries. Continue?")
            },
            confirmButton = {
                TextButton(
                    enabled = !isBusy,
                    onClick = {
                        showCopyOtherModeConfirmation = false
                        scope.launch {
                            isBusy = true
                            try {
                                val otherMode =
                                    if (mode == Type.TIME_TRIAL) Type.GROUP_RACE else Type.TIME_TRIAL
                                val otherEntries = when (otherMode) {
                                    Type.TIME_TRIAL -> startListRepository.getTimeTrialStartList().first()
                                    Type.GROUP_RACE -> startListRepository.getGroupRaceStartList().first()
                                }.sortedBy { it.startOrder }
                                orderedIds = otherEntries.map { it.athleteId }
                                distances = distances + otherEntries.associate {
                                    it.athleteId to it.distanceMeters.toString()
                                }
                                groupOrders = groupOrders + otherEntries.associate {
                                    it.athleteId to it.startOrder.toString()
                                }
                                hasUnsavedChanges = true
                                feedback = "Copied ${otherEntries.size} entries. Save to keep this start list."
                            } catch (exception: Exception) {
                                feedback = exception.message ?: "Could not copy the other start list."
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                ) {
                    Text("Replace")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCopyOtherModeConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showJsonImportConfirmation) {
        AlertDialog(
            onDismissRequest = { showJsonImportConfirmation = false },
            title = { Text("Import JSON start list") },
            text = { Text("This replaces the current start list with entries from a JSON file. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showJsonImportConfirmation = false
                        importLauncher.launch()
                    },
                ) {
                    Text("Choose file")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJsonImportConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    if (showAddAthlete || athleteToEdit != null) {
        AthleteEditorDialog(
            athlete = athleteToEdit,
            onDismiss = {
                showAddAthlete = false
                athleteToEdit = null
            },
            onSave = { athlete ->
                scope.launch {
                    try {
                        if (athlete.id == 0L) {
                            val id = athletesRepository.insert(athlete)
                            orderedIds = orderedIds + id
                            distances = distances + (id to "0")
                            if (mode == Type.GROUP_RACE) {
                                val nextGroup =
                                    (groupOrders.values.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: 0) + 1
                                groupOrders = groupOrders + (id to nextGroup.toString())
                            }
                        } else {
                            athletesRepository.update(athlete)
                        }
                        hasUnsavedChanges = true
                        feedback = null
                    } catch (exception: Exception) {
                        feedback = exception.message ?: "Could not save athlete."
                    }
                }
                showAddAthlete = false
                athleteToEdit = null
            },
        )
    }
}
