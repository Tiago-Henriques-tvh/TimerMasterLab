package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.SessionResult
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionResultDao {
    @Insert
    suspend fun insertSession(sessionResult: SessionResult): Long

    @Update
    suspend fun upsertSession(sessionResult: SessionResult)

    @Delete
    suspend fun deleteSession(sessionResult: SessionResult)

    @Query("SELECT * FROM session_result WHERE sessionId = :sessionId")
    fun getSessionResultsBySessionId(sessionId: Long): Flow<List<SessionResult>>
}
