package com.tiagohenriques.project.timermasterlab.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
)
data class StartListEntry(
    @PrimaryKey @ColumnInfo(name = "athlete_id") val athleteId: Long,
    @ColumnInfo(name = "start_order") val startOrder: Int,
    @ColumnInfo(name = "distance_meters") val distanceMeters: Float,
)
