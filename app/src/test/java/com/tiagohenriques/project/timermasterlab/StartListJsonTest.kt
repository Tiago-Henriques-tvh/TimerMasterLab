package com.tiagohenriques.project.timermasterlab

import com.tiagohenriques.project.timermasterlab.data.StartListFileEntry
import com.tiagohenriques.project.timermasterlab.data.StartListJson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StartListJsonTest {
    @Test
    fun roundTripKeepsAthletesAndMode() {
        val entries =
            listOf(
                StartListFileEntry(
                    name = "Ana Silva",
                    age = 14,
                    gender = "F",
                    category = 3,
                    startOrder = 2,
                    distanceMeters = 1500f,
                ),
            )

        val decoded =
            StartListJson.decode(
                StartListJson.encode(
                    mode = "Time Trial",
                    entries = entries,
                    exportedAtEpochMillis = 1_700_000_000_000L,
                ),
            )

        assertEquals(StartListJson.CURRENT_VERSION, decoded.version)
        assertEquals("Time Trial", decoded.mode)
        assertEquals(1_700_000_000_000L, decoded.exportedAtEpochMillis)
        assertEquals(entries, decoded.entries)
    }

    @Test
    fun rejectsInvalidJson() {
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode("{not json")
        }
    }

    @Test
    fun rejectsBlankAthleteName() {
        val json =
            """
            {"version":1,"mode":"Time Trial","exportedAtEpochMillis":1,
            "entries":[{"name":"  ","age":10,"gender":"M","category":1,"startOrder":1,"distanceMeters":100}]}
            """.trimIndent()

        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode(json)
        }
    }

    @Test
    fun stripsBomAndNormalizesEntryFields() {
        val json =
            """
            {"version":1,"mode":"Time Trial","entries":[
              {"name":" Ana ","age":-2,"gender":" F ","category":3,"startOrder":0,"distanceMeters":-10}
            ]}
            """.trimIndent()

        val decoded = StartListJson.decode("\uFEFF$json")

        assertEquals(
            StartListFileEntry(
                name = "Ana",
                age = 0,
                gender = "F",
                category = 3,
                startOrder = 1,
                distanceMeters = 0f,
            ),
            decoded.entries.single(),
        )
    }

    @Test
    fun rejectsEmptyInputAndUnsupportedVersions() {
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode(" \n ")
        }
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode("""{"version":0,"entries":[]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode("""{"version":${StartListJson.CURRENT_VERSION + 1},"entries":[]}""")
        }
    }

    @Test
    fun rejectsNullEntryDataInsteadOfCrashing() {
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode("""{"version":1,"entries":null}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode("""{"version":1,"entries":[null]}""")
        }
        assertThrows(IllegalArgumentException::class.java) {
            StartListJson.decode(
                """{"version":1,"entries":[{"name":null,"age":10,"gender":"M","category":1,"startOrder":1,"distanceMeters":100}]}""",
            )
        }
    }
}
