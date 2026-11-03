package com.tiagohenriques.project.timermasterlab.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trial_session")
data class Session(
    @PrimaryKey(autoGenerate = true) val sessionId: Long = 0,
    @ColumnInfo(name = "date") val date: String,
)
