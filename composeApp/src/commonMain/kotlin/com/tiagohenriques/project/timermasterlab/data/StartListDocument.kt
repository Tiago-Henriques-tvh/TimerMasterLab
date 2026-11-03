package com.tiagohenriques.project.timermasterlab.data

import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class StartListFileEntry(
    val name: String,
    val age: Int,
    val gender: String,
    val category: Int,
    val startOrder: Int,
    val distanceMeters: Float,
)

@Serializable
data class StartListDocument(
    val version: Int = CURRENT_VERSION,
    val mode: String = "",
    val exportedAtEpochMillis: Long = 0L,
    val entries: List<StartListFileEntry> = emptyList(),
) {
    companion object {
        const val CURRENT_VERSION = 1
        private val json = Json { ignoreUnknownKeys = true }

        fun encode(mode: Type, entries: List<StartListEntry>, athletes: Map<Long, Athlete>, exportedAtEpochMillis: Long): String {
            val documentEntries = entries.sortedBy { it.startOrder }.mapNotNull { entry ->
                val athlete = athletes[entry.athleteId] ?: return@mapNotNull null
                StartListFileEntry(
                    name = athlete.name,
                    age = athlete.age,
                    gender = athlete.gender,
                    category = athlete.category,
                    startOrder = entry.startOrder,
                    distanceMeters = entry.distanceMeters,
                )
            }
            return json.encodeToString(serializer(), StartListDocument(
                mode = mode.displayName,
                exportedAtEpochMillis = exportedAtEpochMillis,
                entries = documentEntries,
            ))
        }

        fun decode(rawJson: String): StartListDocument {
            val trimmed = rawJson.trim().removePrefix("\uFEFF")
            require(trimmed.isNotEmpty()) { "Start list file is empty." }
            val parsed = try {
                json.decodeFromString(serializer(), trimmed)
            } catch (exception: SerializationException) {
                throw IllegalArgumentException("Start list file is not valid JSON.", exception)
            }
            require(parsed.version == CURRENT_VERSION) { "Unsupported start list version: ${parsed.version}." }
            require(parsed.entries.size <= MAX_ENTRIES) { "Start list has too many entries." }
            val cleaned = parsed.entries.map { entry ->
                require(entry.name.isNotBlank()) { "A start-list entry is missing a name." }
                require(entry.distanceMeters.isFinite()) { "A start-list distance is invalid." }
                entry.copy(
                    name = entry.name.trim(),
                    age = entry.age.coerceAtLeast(0),
                    gender = entry.gender.trim(),
                    startOrder = entry.startOrder.coerceAtLeast(1),
                    distanceMeters = entry.distanceMeters.coerceAtLeast(0f),
                )
            }
            return parsed.copy(entries = cleaned)
        }

        private const val MAX_ENTRIES = 10_000
    }
}
