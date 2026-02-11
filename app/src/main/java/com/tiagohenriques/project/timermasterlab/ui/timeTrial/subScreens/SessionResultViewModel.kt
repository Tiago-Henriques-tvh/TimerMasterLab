package com.tiagohenriques.project.timermasterlab.ui.timeTrial.subScreens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.repositories.AthletesRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionResultRepository
import com.tiagohenriques.project.timermasterlab.data.repositories.SessionRepository
import com.tiagohenriques.project.timermasterlab.ui.utils.Utils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SessionResultViewModel(
    private val sessionResultRepository: SessionResultRepository,
    private val sessionRepository: SessionRepository,
    private val athletesRepository: AthletesRepository,
) : ViewModel() {
    fun getSessionResultsBySessionId(sessionId: Long) = sessionResultRepository.getSessionResultsBySessionId(sessionId)

    fun getAllTrialSessions() = sessionRepository.getAllTrialSessions()

    fun getAthleteById(id: Long) = athletesRepository.getAthleteById(id)

    fun exportResultsToPdf(
        context: Context,
        results: List<Pair<SessionResult, Athlete?>>,
        groupBy: String? = null,
        title: String = "Session Results"
    ) {
        if (results.isEmpty()) {
            Toast.makeText(context, "No results to export", Toast.LENGTH_SHORT).show()
            return
        }

        val pdfDocument = android.graphics.pdf.PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()

        val marginLeft = 40f
        val marginTop = 40f
        val lineHeight = 18f
        val headerLineHeight = 22f
        val groupGap = 14f

        val posX = marginLeft
        val nameX = marginLeft + 40f
        val timeX = (pageWidth - marginLeft) - 120f

        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.BLACK
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
                "Category" -> results.groupBy { (_, athlete) -> athlete?.let { Category.fromCode(it.category) } ?: "Unknown" }
                "Distance" -> results.groupBy { (result, _) -> result.distance.toString() }
                else -> results.groupBy { "Unknown" }
            }

            val sortedGroupKeys = grouped.keys.sortedWith(compareBy { key ->
                key.toIntOrNull() ?: Int.MAX_VALUE
            })

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
            "session_results_${System.currentTimeMillis()}.pdf"
        )

        try {
            pdfDocument.writeTo(FileOutputStream(file))
            Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(context, "Error saving PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        } finally {
            pdfDocument.close()
        }
    }
}
