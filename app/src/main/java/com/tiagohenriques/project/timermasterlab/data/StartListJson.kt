package com.tiagohenriques.project.timermasterlab.data

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName

data class StartListFileEntry(
    @SerializedName("name") val name: String,
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String,
    @SerializedName("category") val category: Int,
    @SerializedName("startOrder") val startOrder: Int,
    @SerializedName("distanceMeters") val distanceMeters: Float,
)

data class StartListDocument(
    @SerializedName("version") val version: Int = 1,
    @SerializedName("mode") val mode: String = "",
    @SerializedName("exportedAtEpochMillis") val exportedAtEpochMillis: Long = 0L,
    @SerializedName("entries") val entries: List<StartListFileEntry> = emptyList(),
)

object StartListJson {
    private val gson = Gson()

    fun encode(
        mode: String,
        entries: List<StartListFileEntry>,
        exportedAtEpochMillis: Long,
    ): String {
        val document = StartListDocument(
            version = CURRENT_VERSION,
            mode = mode,
            exportedAtEpochMillis = exportedAtEpochMillis,
            entries = entries,
        )
        return gson.toJson(document)
    }

    fun decode(rawJson: String): StartListDocument {
        val json = rawJson.trim().removePrefix("\uFEFF")
        if (json.isEmpty()) {
            throw IllegalArgumentException("Start list file is empty")
        }
        val parsed = try {
            gson.fromJson(json, StartListDocument::class.java)
        } catch (error: JsonSyntaxException) {
            throw IllegalArgumentException("Start list file is not valid JSON", error)
        } ?: throw IllegalArgumentException("Start list file is empty")

        if (parsed.version < 1) {
            throw IllegalArgumentException("Unsupported start list version")
        }
        val entries = parsed.entries.map { entry ->
            if (entry.name.isBlank()) {
                throw IllegalArgumentException("Start list entry is missing a name")
            }
            entry.copy(
                name = entry.name.trim(),
                gender = entry.gender.trim(),
                startOrder = entry.startOrder.coerceAtLeast(1),
                distanceMeters = entry.distanceMeters.coerceAtLeast(0f),
                age = entry.age.coerceAtLeast(0),
            )
        }
        return parsed.copy(entries = entries)
    }

    const val CURRENT_VERSION = 1
}
