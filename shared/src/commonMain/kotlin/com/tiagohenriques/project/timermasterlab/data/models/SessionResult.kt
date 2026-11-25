package com.tiagohenriques.project.timermasterlab.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "session_result",
    primaryKeys = ["sessionId", "athleteId"],
    foreignKeys = [
        ForeignKey(
            entity = Session::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Athlete::class,
            parentColumns = ["id"],
            childColumns = ["athleteId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("sessionId"),
        Index("athleteId"),
    ],
)
data class SessionResult(
    val sessionId: Long,
    val athleteId: Long,
    val startOrder: Int,
    val distance: Int,
    val startedAt: Long?,
    val duration: Long?,
)
