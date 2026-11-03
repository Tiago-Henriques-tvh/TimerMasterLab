package com.tiagohenriques.project.timermasterlab.data.repositories

import com.tiagohenriques.project.timermasterlab.data.dao.StartListEntryDao
import com.tiagohenriques.project.timermasterlab.data.database.AppDatabase
import com.tiagohenriques.project.timermasterlab.data.database.withSessionTransaction
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import com.tiagohenriques.project.timermasterlab.data.models.Type
import kotlinx.coroutines.flow.Flow

interface StartListEntryRepository {
    suspend fun insert(startList: StartListEntry): Long
    suspend fun update(startList: StartListEntry)
    suspend fun delete(startList: StartListEntry)
    suspend fun clearTimeTrialStartList()
    suspend fun clearGroupRaceStartList()
    suspend fun replaceStartList(type: Type, entries: List<StartListEntry>)
    fun getTimeTrialStartList(): Flow<List<StartListEntry>>
    fun getGroupRaceStartList(): Flow<List<StartListEntry>>
}

class OfflineStartListEntryRepository(
    private val starListEntryDao: StartListEntryDao,
    private val database: AppDatabase,
) : StartListEntryRepository {
    override suspend fun insert(startList: StartListEntry): Long =
        starListEntryDao.insertListEntry(startList)

    override suspend fun update(startList: StartListEntry) = starListEntryDao.updateListEntry(startList)

    override suspend fun delete(startList: StartListEntry) = starListEntryDao.deleteListEntry(startList)

    override suspend fun clearTimeTrialStartList() = starListEntryDao.clearTimeTrialStartList()

    override suspend fun clearGroupRaceStartList() = starListEntryDao.clearGroupRaceStartList()

    override suspend fun replaceStartList(type: Type, entries: List<StartListEntry>) {
        require(entries.all { it.type == type.displayName }) {
            "All start-list entries must use the selected race type."
        }
        database.withSessionTransaction {
            when (type) {
                Type.TIME_TRIAL -> starListEntryDao.clearTimeTrialStartList()
                Type.GROUP_RACE -> starListEntryDao.clearGroupRaceStartList()
            }
            entries.forEach { starListEntryDao.insertListEntry(it) }
        }
    }

    override fun getTimeTrialStartList(): Flow<List<StartListEntry>> =
        starListEntryDao.getTimeTrialStartList()

    override fun getGroupRaceStartList(): Flow<List<StartListEntry>> =
        starListEntryDao.getGroupRaceStartList()
}
