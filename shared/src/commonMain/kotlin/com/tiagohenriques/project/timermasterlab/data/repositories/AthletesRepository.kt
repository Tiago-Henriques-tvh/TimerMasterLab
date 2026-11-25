package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.AthletesDao
import com.tiagohenriques.project.timermasterlab.data.models.Athlete
import kotlinx.coroutines.flow.Flow

interface AthletesRepository {
    suspend fun insert(athlete: Athlete): Long
    suspend fun update(athlete: Athlete)
    suspend fun delete(athlete: Athlete)
    fun getAthleteById(id: Long): Flow<Athlete>
    fun getAllAthletes(): Flow<List<Athlete>>
    suspend fun getAllAthletesOnce(): List<Athlete>
}

class OfflineAthletesRepository(
    private val athleteDao: AthletesDao,
) : AthletesRepository {
    override suspend fun insert(athlete: Athlete): Long = athleteDao.insertAthlete(athlete)

    override suspend fun update(athlete: Athlete) = athleteDao.updateAthlete(athlete)

    override suspend fun delete(athlete: Athlete) = athleteDao.deleteAthlete(athlete)

    override fun getAthleteById(id: Long): Flow<Athlete> = athleteDao.getAthleteById(id)

    override fun getAllAthletes(): Flow<List<Athlete>> = athleteDao.getAllAthletes()

    override suspend fun getAllAthletesOnce(): List<Athlete> = athleteDao.getAllAthletesOnce()
}
