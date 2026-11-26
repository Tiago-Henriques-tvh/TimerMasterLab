package com.tiagohenriques.project.timermasterlab.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.tiagohenriques.project.timermasterlab.data.models.StartListEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StartListEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListEntry(entry: StartListEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListEntries(entries: List<StartListEntry>)

    @Update
    suspend fun updateListEntry(entry: StartListEntry)

    @Delete
    suspend fun deleteListEntry(entry: StartListEntry)

    @Query("DELETE FROM start_list_entry WHERE type = 'Time Trial'")
    suspend fun clearTimeTrialStartList()

    @Query("DELETE FROM start_list_entry WHERE type = 'Group Race'")
    suspend fun clearGroupRaceStartList()

    @Transaction
    suspend fun replaceTimeTrialStartList(entries: List<StartListEntry>) {
        clearTimeTrialStartList()
        insertListEntries(entries)
    }

    @Transaction
    suspend fun replaceGroupRaceStartList(entries: List<StartListEntry>) {
        clearGroupRaceStartList()
        insertListEntries(entries)
    }

    @Query("SELECT * FROM start_list_entry WHERE type = 'Time Trial' ORDER BY start_order ASC")
    fun getTimeTrialStartList(): Flow<List<StartListEntry>>

    @Query("SELECT * FROM start_list_entry WHERE type = 'Time Trial' ORDER BY start_order ASC")
    suspend fun getTimeTrialStartListOnce(): List<StartListEntry>

    @Query("SELECT * FROM start_list_entry WHERE type = 'Group Race' ORDER BY start_order ASC")
    fun getGroupRaceStartList(): Flow<List<StartListEntry>>

    @Query("SELECT * FROM start_list_entry WHERE type = 'Group Race' ORDER BY start_order ASC")
    suspend fun getGroupRaceListOnce(): List<StartListEntry>
}
