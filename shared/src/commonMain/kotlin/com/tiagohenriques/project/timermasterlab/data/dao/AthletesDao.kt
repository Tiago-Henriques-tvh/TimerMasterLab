package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import kotlinx.coroutines.flow.Flow

@Dao
interface AthletesDao {
    @Insert
    suspend fun insertAthlete(athlete: Athlete): Long

    @Update
    suspend fun updateAthlete(athlete: Athlete)

    @Delete
    suspend fun deleteAthlete(athlete: Athlete)

    @Query("SELECT * FROM athlete WHERE id = :id")
    fun getAthleteById(id: Long): Flow<Athlete>

    @Query("SELECT * FROM athlete ORDER BY name COLLATE NOCASE")
    fun getAllAthletes(): Flow<List<Athlete>>

    @Query("SELECT * FROM athlete")
    suspend fun getAllAthletesOnce(): List<Athlete>
}
