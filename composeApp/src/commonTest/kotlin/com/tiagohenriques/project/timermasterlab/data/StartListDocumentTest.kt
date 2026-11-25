package com.tiagohenriques.project.timermasterlab.data

import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import com.tiagohenriques.project.timermasterlab.ui.createStartListPdf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class StartListDocumentTest {
    @Test
    fun exportedStartListJsonCanBeImported() {
        val athlete = Athlete(
            id = 4,
            name = "José Runner",
            age = 21,
            gender = "Other",
            category = 5,
        )
        val entry = StartListEntry(
            athleteId = athlete.id,
            startOrder = 2,
            distanceMeters = 1500f,
            type = Type.TIME_TRIAL.displayName,
        )

        val json = StartListDocument.encode(
            mode = Type.TIME_TRIAL,
            entries = listOf(entry),
            athletes = mapOf(athlete.id to athlete),
            exportedAtEpochMillis = 1234L,
        )
        val imported = StartListDocument.decode(json)

        assertEquals(1, imported.version)
        assertEquals(Type.TIME_TRIAL.displayName, imported.mode)
        assertEquals(1234L, imported.exportedAtEpochMillis)
        assertEquals("José Runner", imported.entries.single().name)
        assertEquals(2, imported.entries.single().startOrder)
        assertEquals(1500f, imported.entries.single().distanceMeters)
    }

    @Test
    fun unsupportedJsonVersionsAreRejected() {
        assertFailsWith<IllegalArgumentException> {
            StartListDocument.decode("""{"version":99,"entries":[]}""")
        }
    }

    @Test
    fun groupRaceJsonPreservesSharedStartGroups() {
        val athletes = listOf(
            Athlete(id = 1, name = "First", age = 20, gender = "Other", category = 1),
            Athlete(id = 2, name = "Second", age = 21, gender = "Other", category = 1),
        )
        val entries = athletes.map { athlete ->
            StartListEntry(
                athleteId = athlete.id,
                startOrder = 1,
                distanceMeters = 1000f,
                type = Type.GROUP_RACE.displayName,
            )
        }

        val document = StartListDocument.decode(
            StartListDocument.encode(
                mode = Type.GROUP_RACE,
                entries = entries,
                athletes = athletes.associateBy { it.id },
                exportedAtEpochMillis = 1234L,
            ),
        )

        assertEquals(listOf(1, 1), document.entries.map { it.startOrder })
    }

    @Test
    fun startListPdfHasValidHeaderAndXref() {
        val athlete = Athlete(
            id = 1,
            name = "Ana Runner",
            age = 19,
            gender = "Female",
            category = 4,
        )
        val bytes = createStartListPdf(
            entries = listOf(
                StartListEntry(
                    athleteId = athlete.id,
                    startOrder = 1,
                    distanceMeters = 5000f,
                    type = Type.GROUP_RACE.displayName,
                ),
            ),
            athletes = mapOf(athlete.id to athlete),
            title = "Group Race Start List",
        )
        val pdf = bytes.decodeToString()
        val xrefOffset = Regex("startxref\\n(\\d+)").find(pdf)?.groupValues?.get(1)?.toInt()

        assertTrue(pdf.startsWith("%PDF-1.4"))
        assertTrue("Ana Runner" in pdf)
        assertTrue(xrefOffset != null)
        assertEquals("xref", pdf.substring(xrefOffset!!, xrefOffset + 4))
    }
}
