package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.StartListEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StartListUiState(
    val draft: List<DraftStartListItem> = emptyList(),
    val hasUnsavedChanges: Boolean = false,
    val isLoading: Boolean = false,
)

data class DraftStartListItem(
    val athlete: Athlete,
    val startOrder: Int,
    val distanceMeters: Float,
)

class StartListViewModel(
    private val athletesRepository: AthletesRepository,
    private val startListEntryRepository: StartListEntryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StartListUiState())
    val uiState: StateFlow<StartListUiState> = _uiState.asStateFlow()
    private var currentMode: Type = Type.TIME_TRIAL

    private fun startListFlowFor(mode: Type): Flow<List<StartListEntry>> {
        return if (mode == Type.GROUP_RACE) {
            startListEntryRepository.getGroupRaceStartList()
        } else {
            startListEntryRepository.getTimeTrialStartList()
        }
    }

    fun importStartListFrom(fromMode: Type) {
        if (fromMode == currentMode) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val athletesMap = athletesRepository.getAllAthletesOnce().associateBy { it.id }
                val fromEntries = startListFlowFor(fromMode).first()

                val draftItems = fromEntries.mapNotNull { entry ->
                    val athlete = athletesMap[entry.athleteId] ?: return@mapNotNull null
                    DraftStartListItem(
                        athlete = athlete,
                        startOrder = entry.startOrder,
                        distanceMeters = entry.distanceMeters,
                    )
                }

                _uiState.value = StartListUiState(
                    draft = draftItems,
                    hasUnsavedChanges = true,
                    isLoading = false,
                )
            } catch (_: Throwable) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadStartListForMode(mode: String) {
        Log.d("StartListViewModel", "Loading start list for mode: $mode")
        _uiState.update { it.copy(isLoading = true) }

        currentMode = Type.fromStringToType(mode.replace("_", " "))!!
        viewModelScope.launch {
            val flow = startListFlowFor(currentMode)

            flow.take(1).collect { entries ->
                Log.d("StartListViewModel", "Loaded ${entries.size} entries: $entries")

                val athletesMap = athletesRepository.getAllAthletesOnce().associateBy { it.id }

                val draftItems = entries.mapNotNull { entry ->
                    val athlete = athletesMap[entry.athleteId] ?: return@mapNotNull null
                    DraftStartListItem(
                        athlete = athlete,
                        startOrder = entry.startOrder,
                        distanceMeters = entry.distanceMeters,
                    )
                }

                _uiState.value = StartListUiState(draft = draftItems)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun addAthleteToDraft(
        athlete: Athlete,
        distanceMeters: Float,
        startOrder: Int? = null,
    ) {
        viewModelScope.launch {
            val athleteId = athletesRepository.insert(athlete)
            val savedAthlete = athlete.copy(id = athleteId)

            _uiState.update { state ->
                if (currentMode == Type.GROUP_RACE) {
                    Log.d("StartListViewModel", "Adding athlete to GROUP_RACE draft")
                    val assignedOrder = startOrder?.coerceAtLeast(1) ?: (state.draft.size + 1)

                    state.copy(
                        draft = state.draft + DraftStartListItem(
                            athlete = savedAthlete,
                            startOrder = assignedOrder,
                            distanceMeters = distanceMeters
                        ),
                        hasUnsavedChanges = true
                    )
                } else {
                    Log.d("StartListViewModel", "Adding athlete to TIME_TRIAL draft")
                    val safeStartOrder = startOrder?.coerceAtLeast(1)
                    val insertIndex = safeStartOrder?.let { (it - 1).coerceIn(0, state.draft.size) } ?: state.draft.size

                    val newDraft = state.draft.toMutableList().apply {
                        add(
                            insertIndex, DraftStartListItem(
                                athlete = savedAthlete,
                                startOrder = insertIndex + 1,
                                distanceMeters = distanceMeters
                            )
                        )
                    }.mapIndexed { index, item -> item.copy(startOrder = index + 1) }

                    state.copy(draft = newDraft, hasUnsavedChanges = true)
                }
            }
        }
    }

    fun removeDraftItem(athleteId: Long) {
        _uiState.update { state ->
            val updated = state.draft.filterNot { it.athlete.id == athleteId }

            val resulting = if (currentMode == Type.GROUP_RACE) {
                updated
            } else {
                updated.mapIndexed { i, item -> item.copy(startOrder = i + 1) }
            }

            state.copy(draft = resulting, hasUnsavedChanges = true)
        }
    }

    fun updateDraftItem(updated: DraftStartListItem) {
        viewModelScope.launch {
            athletesRepository.update(updated.athlete)

            _uiState.update { state ->
                if (currentMode == Type.GROUP_RACE) {
                    val replaced = state.draft.map { item ->
                        if (item.athlete.id == updated.athlete.id) {
                            item.copy(
                                athlete = updated.athlete,
                                distanceMeters = updated.distanceMeters,
                                startOrder = updated.startOrder
                            )
                        } else item
                    }

                    state.copy(draft = replaced, hasUnsavedChanges = true)
                } else {
                    val withoutItem = state.draft.filterNot {
                        it.athlete.id == updated.athlete.id
                    }

                    val safeOrder = updated.startOrder.coerceAtLeast(1)
                    val insertIndex = (safeOrder - 1)
                        .coerceIn(0, withoutItem.size)

                    val reordered = withoutItem
                        .toMutableList()
                        .apply { add(insertIndex, updated) }
                        .mapIndexed { index, item ->
                            item.copy(startOrder = index + 1)
                        }

                    state.copy(
                        draft = reordered,
                        hasUnsavedChanges = true
                    )
                }
            }
        }
    }

    fun commitStartList() = viewModelScope.launch {
        if (currentMode == Type.TIME_TRIAL) {
            Log.d("StartListViewModel", "Clearing time trial start list to save a new one")
            startListEntryRepository.clearTimeTrialStartList()
        } else if (currentMode == Type.GROUP_RACE) {
            Log.d("StartListViewModel", "Clearing group race start list to save a new one")
            startListEntryRepository.clearGroupRaceStartList()
        }

        uiState.value.draft.forEach { item ->
            startListEntryRepository.insert(
                StartListEntry(
                    athleteId = item.athlete.id,
                    startOrder = item.startOrder,
                    distanceMeters = item.distanceMeters,
                    type = Type.fromTypeToString(currentMode) ?: ""
                )
            )
        }

        _uiState.update { it.copy(hasUnsavedChanges = false) }
    }

    fun moveDraftItem(fromIndex: Int, toIndex: Int) {
        val oldDraft = uiState.value.draft
        val size = oldDraft.size
        if (fromIndex !in 0 until size || toIndex !in 0..size || fromIndex == toIndex) return

        val newDraft = oldDraft.toMutableList().apply {
            val item = removeAt(fromIndex)
            add(toIndex, item)
        }

        val resulting = if (currentMode == Type.GROUP_RACE) {
            newDraft
        } else {
            newDraft.mapIndexed { index, draftItem -> draftItem.copy(startOrder = index + 1) }
        }

        _uiState.update { it.copy(draft = resulting, hasUnsavedChanges = true) }
    }

    fun exportStartListToPDF(context: Context) = viewModelScope.launch {
        val draft = uiState.value.draft
        if (draft.isEmpty()) {
            Toast.makeText(context, "No athletes to export", Toast.LENGTH_SHORT).show()
            return@launch
        }

        withContext(Dispatchers.IO) {
            try {
                val pageWidth = 595
                val pageHeight = 842
                val margin = 40
                val lineHeight = 24

                val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val fileName = "startlist_${sdf.format(Date())}.pdf"

                val pdf = PdfDocument()
                var pageIndex = 0
                var yPos = margin

                fun startNewPage(): PdfDocument.Page {
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
                    pageIndex += 1
                    yPos = margin
                    return pdf.startPage(pageInfo)
                }

                var currentPage = startNewPage()
                val canvas = currentPage.canvas
                val paint = Paint().apply {
                    isAntiAlias = true
                    color = Color.BLACK
                    textSize = 18f
                    typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                }

                canvas.drawText("Start List", margin.toFloat(), yPos.toFloat(), paint)
                yPos += lineHeight * 2

                paint.textSize = 14f
                paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
                val colOrder = margin.toFloat()
                val colName = 80f
                val colDistance = 280f
                val colAge = 360f
                val colGender = 420f
                val colCategory = 480f

                canvas.drawText("No", colOrder, yPos.toFloat(), paint)
                canvas.drawText("Name", colName, yPos.toFloat(), paint)
                canvas.drawText("Distance", colDistance, yPos.toFloat(), paint)
                canvas.drawText("Age", colAge, yPos.toFloat(), paint)
                canvas.drawText("Gender", colGender, yPos.toFloat(), paint)
                canvas.drawText("Category", colCategory, yPos.toFloat(), paint)
                yPos += lineHeight

                paint.textSize = 12f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

                val linesPerPage = (pageHeight - margin * 2) / lineHeight - 3
                var lineCount = 0

                draft.forEach { item ->
                    if (lineCount >= linesPerPage) {
                        pdf.finishPage(currentPage)
                        currentPage = startNewPage()
                        lineCount = 0
                    }

                    val athlete = item.athlete
                    val categoryName = runCatching { Category.fromCode(athlete.category) }.getOrNull() ?: ""

                    canvas.drawText(item.startOrder.toString(), colOrder, yPos.toFloat(), paint)
                    canvas.drawText(athlete.name, colName, yPos.toFloat(), paint)
                    canvas.drawText("${item.distanceMeters.toInt()}m", colDistance, yPos.toFloat(), paint)
                    canvas.drawText("${athlete.age}", colAge, yPos.toFloat(), paint)
                    canvas.drawText(athlete.gender, colGender, yPos.toFloat(), paint)
                    canvas.drawText(categoryName, colCategory, yPos.toFloat(), paint)

                    yPos += lineHeight
                    lineCount++
                }

                pdf.finishPage(currentPage)

                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )

                FileOutputStream(file).use { pdf.writeTo(it) }
                pdf.close()

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
