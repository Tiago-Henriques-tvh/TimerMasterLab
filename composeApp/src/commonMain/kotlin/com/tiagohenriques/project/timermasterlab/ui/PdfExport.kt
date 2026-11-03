package com.tiagohenriques.project.timermasterlab.ui

import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.Category
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry

internal data class PdfLine(
    val x: Int,
    val y: Int,
    val text: String,
    val size: Int = 10,
    val bold: Boolean = false,
)

internal fun createStartListPdf(
    entries: List<StartListEntry>,
    athletes: Map<Long, Athlete>,
    title: String,
): ByteArray {
    val rows = entries.sortedBy { it.startOrder }.mapNotNull { entry ->
        val athlete = athletes[entry.athleteId] ?: return@mapNotNull null
        listOf(
            PdfLine(40, 0, entry.startOrder.toString()),
            PdfLine(78, 0, athlete.name),
            PdfLine(300, 0, "${entry.distanceMeters.toInt()} m"),
            PdfLine(378, 0, athlete.age.toString()),
            PdfLine(422, 0, athlete.gender),
            PdfLine(480, 0, Category.fromCode(athlete.category)),
        )
    }
    val pages = mutableListOf<List<PdfLine>>()
    var lines = mutableListOf(PdfLine(40, 800, title, size = 18, bold = true))
    var y = 770
    fun addHeader() {
        lines += listOf(
            PdfLine(40, y, "No", bold = true),
            PdfLine(78, y, "Name", bold = true),
            PdfLine(300, y, "Distance", bold = true),
            PdfLine(378, y, "Age", bold = true),
            PdfLine(422, y, "Gender", bold = true),
            PdfLine(480, y, "Category", bold = true),
        )
        y -= 22
    }
    addHeader()
    rows.forEach { row ->
        if (y < 40) {
            pages += lines
            lines = mutableListOf(PdfLine(40, 800, title, size = 18, bold = true))
            y = 770
            addHeader()
        }
        lines += row.map { it.copy(y = y) }
        y -= 20
    }
    pages += lines
    return buildTextPdf(pages)
}

internal fun createSessionResultsPdf(
    title: String,
    sections: List<Pair<String, List<Pair<SessionResult, Athlete?>>>>,
): ByteArray {
    val pages = mutableListOf<List<PdfLine>>()
    var lines = mutableListOf(
        PdfLine(40, 800, title, size = 18, bold = true),
        PdfLine(40, 770, "Pos", bold = true),
        PdfLine(82, 770, "Name", bold = true),
        PdfLine(400, 770, "Distance", bold = true),
        PdfLine(500, 770, "Time", bold = true),
    )
    var y = 748
    var position = 0
    sections.forEach { (sectionTitle, results) ->
        if (sectionTitle.isNotBlank()) {
            if (y < 60) {
                pages += lines
                lines = mutableListOf(PdfLine(40, 800, title, size = 18, bold = true))
                y = 770
            }
            lines += PdfLine(40, y, sectionTitle, size = 12, bold = true)
            y -= 18
        }
        results.forEach { (result, athlete) ->
            if (y < 40) {
                pages += lines
                lines = mutableListOf(PdfLine(40, 800, title, size = 18, bold = true))
                y = 770
            }
            position += 1
            lines += listOf(
                PdfLine(40, y, "$position."),
                PdfLine(82, y, athlete?.name ?: "Athlete ${result.athleteId}"),
                PdfLine(400, y, "${result.distance} m"),
                PdfLine(500, y, formatRaceDuration(result.duration ?: 0L)),
            )
            y -= 18
        }
        y -= 8
    }
    pages += lines
    return buildTextPdf(pages)
}

private fun buildTextPdf(pages: List<List<PdfLine>>): ByteArray {
    val objectBodies = mutableListOf<String>()
    objectBodies += "<< /Type /Catalog /Pages 2 0 R >>"
    val pageObjectIds = pages.indices.map { index -> 6 + index * 2 }
    objectBodies += "<< /Type /Pages /Kids [${pageObjectIds.joinToString(" ") { "$it 0 R" }}] /Count ${pages.size} >>"
    objectBodies += "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"
    objectBodies += "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"
    pages.forEachIndexed { index, pageLines ->
        val commands = buildString {
            pageLines.forEach { line ->
                append("BT /F${if (line.bold) 2 else 1} ${line.size} Tf ")
                append("${line.x} ${line.y} Td (${encodePdfText(line.text)}) Tj ET\n")
            }
        }
        val contentBytes = commands.encodeToByteArray()
        objectBodies += "<< /Length ${contentBytes.size} >>\nstream\n$commands" + "endstream"
        val contentObjectId = 5 + index * 2
        objectBodies += "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R /F2 4 0 R >> >> /Contents $contentObjectId 0 R >>"
    }
    val document = StringBuilder("%PDF-1.4\n")
    val offsets = mutableListOf(0)
    objectBodies.forEachIndexed { index, body ->
        offsets += document.toString().encodeToByteArray().size
        document.append("${index + 1} 0 obj\n$body\nendobj\n")
    }
    val xrefOffset = document.toString().encodeToByteArray().size
    document.append("xref\n0 ${objectBodies.size + 1}\n")
    document.append("0000000000 65535 f \n")
    offsets.drop(1).forEach { offset -> document.append(offset.toString().padStart(10, '0')).append(" 00000 n \n") }
    document.append("trailer\n<< /Size ${objectBodies.size + 1} /Root 1 0 R >>\n")
    document.append("startxref\n$xrefOffset\n%%EOF\n")
    return document.toString().encodeToByteArray()
}

private fun encodePdfText(text: String): String = buildString {
    text.forEach { char ->
        val code = char.code
        val byteValue = when {
            code in 32..126 -> code
            code in 160..255 -> code
            char == '\u20AC' -> 128
            char == '\u201A' -> 130
            char == '\u0192' -> 131
            char == '\u201E' -> 132
            char == '\u2026' -> 133
            char == '\u2020' -> 134
            char == '\u2021' -> 135
            char == '\u02C6' -> 136
            char == '\u2030' -> 137
            char == '\u0160' -> 138
            char == '\u2039' -> 139
            char == '\u0152' -> 140
            char == '\u017D' -> 142
            char == '\u2018' -> 145
            char == '\u2019' -> 146
            char == '\u201C' -> 147
            char == '\u201D' -> 148
            char == '\u2022' -> 149
            char == '\u2013' -> 150
            char == '\u2014' -> 151
            char == '\u02DC' -> 152
            char == '\u2122' -> 153
            char == '\u0161' -> 154
            char == '\u203A' -> 155
            char == '\u0153' -> 156
            char == '\u017E' -> 158
            char == '\u0178' -> 159
            else -> '?'.code
        }
        when (byteValue) {
            '\\'.code, '('.code, ')'.code -> append('\\').append(byteValue.toChar())
            in 32..126 -> append(byteValue.toChar())
            else -> append('\\').append(byteValue.toString(8).padStart(3, '0'))
        }
    }
}
