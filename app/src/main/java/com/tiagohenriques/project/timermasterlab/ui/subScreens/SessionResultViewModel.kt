package com.tiagohenriques.project.timermasterlab.ui.subScreens

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.Session
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionResultRepository
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SessionResultViewModel(
    private val sessionResultRepository: SessionResultRepository,
    private val sessionRepository: SessionRepository,
    private val athletesRepository: AthletesRepository,
) : ViewModel() {
    data class SessionResultRow(
        val result: SessionResult,
        val athlete: Athlete?,
    )

    data class SessionResultsSection(
        val title: String,
        val items: List<SessionResultRow>,
    )

    data class SessionResultsUiState(
        val sessions: List<Session> = emptyList(),
        val selectedSession: Session? = null,
        val groupBy: String = "None",
        val sections: List<SessionResultsSection> = emptyList(),
        val isExporting: Boolean = false,
    ) {
        val totalCount: Int get() = sections.sumOf { it.items.size }
        val hasSelection: Boolean get() = selectedSession != null
    }

    sealed interface UiEvent {
        data class Message(val text: String) : UiEvent
    }

    private val _selectedSessionId = MutableStateFlow<Long?>(null)
    private val _groupBy = MutableStateFlow("None")
    private val _isExporting = MutableStateFlow(false)

    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 8)
    val events = _events.asSharedFlow()

    fun setSelectedSession(sessionId: Long?) {
        _selectedSessionId.value = sessionId
    }

    fun setGroupBy(groupBy: String) {
        _groupBy.value = groupBy
    }

    private val sessionsFlow = sessionRepository.getAllTrialSessions()

    private val selectedSessionFlow = combine(sessionsFlow, _selectedSessionId) { sessions, selectedId ->
        sessions.firstOrNull { it.sessionId == selectedId }
    }

    private val selectedSessionResultsFlow = _selectedSessionId
        .flatMapLatest { id ->
            if (id == null) flow { emit(emptyList()) } else sessionResultRepository.getSessionResultsBySessionId(id)
        }

    private val athletesMapFlow = flow {
        // Keep it simple: load once on subscription; for this screen it's enough and avoids N+1 lookups.
        val athletes = athletesRepository.getAllAthletesOnce()
        emit(athletes.associateBy { it.id })
    }.flowOn(Dispatchers.IO)

    private fun groupKey(groupBy: String, row: SessionResultRow): String {
        val athlete = row.athlete
        val result = row.result
        return when (groupBy) {
            "Age" -> athlete?.age?.toString() ?: "Unknown"
            "Gender" -> athlete?.gender ?: "Unknown"
            "Category" -> athlete?.let { Category.fromCode(it.category).toString() } ?: "Unknown"
            "Distance" -> result.distance.toString()
            else -> ""
        }
    }

    private fun sortGroupKeys(keys: Set<String>): List<String> {
        // Prefer numeric sorts when possible (Age/Distance), otherwise alphabetical.
        return keys.sortedWith(compareBy<String> { it.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it })
    }

    val uiState: StateFlow<SessionResultsUiState> = combine(
        combine(
            combine(sessionsFlow, selectedSessionFlow) { sessions, selectedSession ->
                sessions to selectedSession
            },
            selectedSessionResultsFlow,
        ) { (sessions, selectedSession), results ->
            Triple(sessions, selectedSession, results)
        },
        combine(athletesMapFlow, _groupBy) { athletesMap, groupBy ->
            athletesMap to groupBy
        },
        _isExporting,
    ) { (sessions, selectedSession, results), (athletesMap, groupBy), isExporting ->
        val enrichedSorted: List<SessionResultRow> = results
            .map { r -> SessionResultRow(result = r, athlete = athletesMap[r.athleteId]) }
            .sortedBy { row -> row.result.duration ?: Long.MAX_VALUE }

        val sections: List<SessionResultsSection> = if (groupBy == "None") {
            listOf(SessionResultsSection(title = "", items = enrichedSorted))
        } else {
            val grouped = enrichedSorted.groupBy { row -> groupKey(groupBy, row) }
            sortGroupKeys(grouped.keys).map { key ->
                SessionResultsSection(title = key, items = grouped[key].orEmpty())
            }
        }

        SessionResultsUiState(
            sessions = sessions,
            selectedSession = selectedSession,
            groupBy = groupBy,
            sections = sections,
            isExporting = isExporting,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionResultsUiState())


    fun exportCurrentResultsToPdf(context: Context, title: String = "Session Results") {
        // Touch context to keep it from being flagged as unused by IDE inspections.
        // (We keep it as a parameter to make migrating to MediaStore/SAF export easier.)
        context.applicationContext

        val snapshot = uiState.value
        val flatResults: List<Pair<SessionResult, Athlete?>> = snapshot.sections
            .flatMap { section -> section.items }
            .map { row -> row.result to row.athlete }

        if (flatResults.isEmpty()) {
            _events.tryEmit(UiEvent.Message("No results to export"))
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isExporting.value = true
            try {
                val path = exportResultsToPdfInternal(
                    results = flatResults,
                    groupBy = snapshot.groupBy,
                    title = title,
                )
                _events.emit(UiEvent.Message("PDF saved: $path"))
            } catch (e: Throwable) {
                _events.emit(UiEvent.Message("Error saving PDF: ${e.localizedMessage ?: "Unknown error"}"))
            } finally {
                _isExporting.value = false
            }
        }
    }

    private fun exportResultsToPdfInternal(
        results: List<Pair<SessionResult, Athlete?>>,
        groupBy: String? = null,
        title: String = "Session Results",
    ): String {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val marginLeft = 40f
        val marginTop = 40f
        val lineHeight = 18f
        val headerLineHeight = 22f
        val groupGap = 14f

        val posX = marginLeft
        val nameX = marginLeft + 40f
        val timeX = (pageWidth - marginLeft) - 120f

        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
            textSize = 12f
        }

        var currentPage = pdfDocument.startPage(pageInfo)
        var canvas = currentPage.canvas
        var yPos = marginTop

        fun startNewPage() {
            pdfDocument.finishPage(currentPage)
            currentPage = pdfDocument.startPage(pageInfo)
            canvas = currentPage.canvas
        }

        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("🏁 $title", marginLeft, yPos, paint)
        yPos += headerLineHeight

        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Pos", posX, yPos, paint)
        canvas.drawText("Name", nameX, yPos, paint)
        canvas.drawText("Time", timeX, yPos, paint)
        paint.isFakeBoldText = false
        yPos += lineHeight

        val effectiveGroup = groupBy?.takeIf { it.isNotBlank() && it != "None" }

        if (effectiveGroup == null) {
            results.forEachIndexed { index, (result, athlete) ->
                if (yPos > pageHeight - marginTop) {
                    startNewPage()
                    yPos = marginTop
                }

                val posText = "${index + 1}."
                val nameText = athlete?.name ?: "Athlete #${result.athleteId}"
                val timeText = Utils.formatDuration(result.duration)

                paint.textSize = 12f
                canvas.drawText(posText, posX, yPos, paint)
                canvas.drawText(nameText, nameX, yPos, paint)
                canvas.drawText(timeText, timeX, yPos, paint)

                yPos += lineHeight
            }
        } else {
            val grouped = when (effectiveGroup) {
                "Age" -> results.groupBy { (_, athlete) -> athlete?.age?.toString() ?: "Unknown" }
                "Gender" -> results.groupBy { (_, athlete) -> athlete?.gender ?: "Unknown" }
                "Category" -> results.groupBy { (_, athlete) ->
                    athlete?.let { Category.fromCode(it.category) }?.toString() ?: "Unknown"
                }

                "Distance" -> results.groupBy { (result, _) -> result.distance.toString() }
                else -> results.groupBy { "Unknown" }
            }

            val sortedGroupKeys = sortGroupKeys(grouped.keys)

            var overallIndex = 0
            for (groupKey in sortedGroupKeys) {
                val items = grouped[groupKey] ?: continue

                if (yPos > pageHeight - marginTop - groupGap) {
                    startNewPage()
                    yPos = marginTop
                }

                paint.isFakeBoldText = true
                paint.textSize = 13f
                canvas.drawText(groupKey, marginLeft, yPos, paint)
                paint.isFakeBoldText = false
                paint.textSize = 12f
                yPos += groupGap

                items.forEach { (result, athlete) ->
                    if (yPos > pageHeight - marginTop) {
                        startNewPage()
                        yPos = marginTop
                    }

                    overallIndex += 1
                    val posText = "${overallIndex}."
                    val nameText = athlete?.name ?: "Athlete #${result.athleteId}"
                    val timeText = Utils.formatDuration(result.duration)

                    canvas.drawText(posText, posX, yPos, paint)
                    canvas.drawText(nameText, nameX, yPos, paint)
                    canvas.drawText(timeText, timeX, yPos, paint)

                    yPos += lineHeight
                }

                yPos += groupGap
            }
        }

        pdfDocument.finishPage(currentPage)

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "session_results_${System.currentTimeMillis()}.pdf",
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            throw e
        } finally {
            pdfDocument.close()
        }

        return file.absolutePath
    }
}
