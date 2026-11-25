package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.Session
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insertTrialSession(session: Session): Long

    @Update
    suspend fun updateTrialSession(session: Session)

    @Delete
    suspend fun deleteTrialSession(session: Session)

    @Query("SELECT * FROM trial_session WHERE sessionId = :sessionId")
    fun getTrialSessionById(sessionId: Long): Flow<Session>

    @Query("SELECT * FROM trial_session")
    fun getAllTrialSessions(): Flow<List<Session>>
}
