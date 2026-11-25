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
    fun insertAthlete(athlete: Athlete): Long

    @Update
    fun updateAthlete(athlete: Athlete)

    @Delete
    fun deleteAthlete(athlete: Athlete)

    @Query("SELECT * FROM athlete WHERE id = :id")
    fun getAthleteById(id: Long): Flow<Athlete>

    @Query("SELECT * FROM athlete")
    fun getAllAthletesOnce(): List<Athlete>
}
