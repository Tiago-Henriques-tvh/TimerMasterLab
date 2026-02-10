package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.TrialSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TrialSessionDao {
    @Insert
    suspend fun insertTrialSession(trialSession: TrialSession): Long

    @Update
    suspend fun updateTrialSession(trialSession: TrialSession)

    @Delete
    suspend fun deleteTrialSession(trialSession: TrialSession)

    @Query("SELECT * FROM trial_session WHERE sessionId = :sessionId")
    fun getTrialSessionById(sessionId: Long): Flow<TrialSession>

    @Query("SELECT * FROM trial_session")
    fun getAllTrialSessions(): Flow<List<TrialSession>>
}
