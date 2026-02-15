package com.tiagohenriques.project.timermasterlab.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "start_list_entry",
    foreignKeys = [
        ForeignKey(
            entity = Athlete::class,
            parentColumns = ["id"],
            childColumns = ["athlete_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("athlete_id"),
        Index("start_order"),
    ],
    primaryKeys = ["athlete_id", "type"]
)
data class StartListEntry(
    @ColumnInfo(name = "athlete_id") val athleteId: Long,
    @ColumnInfo(name = "start_order") val startOrder: Int,
    @ColumnInfo(name = "distance_meters") val distanceMeters: Float,
    @ColumnInfo(name = "type") val type: String,
)

enum class Type(val displayName: String) {
    TIME_TRIAL("Time Trial"),
    GROUP_RACE("Group Race");

    companion object {
        fun fromStringToType(string: String): Type? {
            return entries.find { it.displayName.equals(string, ignoreCase = true) }
        }

        fun fromTypeToString(type: Type): String? {
            return entries.find { it == type }?.displayName
        }
    }
}
