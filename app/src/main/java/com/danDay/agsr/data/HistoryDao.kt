package com.danDay.agsr.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history_table")
    fun getHistory() : Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history :History)

    @Update
    suspend fun update(history: History)

    @Delete
    suspend fun delete(history: History)
}