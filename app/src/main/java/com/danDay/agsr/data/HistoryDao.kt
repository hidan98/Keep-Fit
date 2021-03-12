package com.danDay.agsr.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history_table")
    fun getHistory() : Flow<List<History>>

    @Query("SELECT * FROM history_table WHERE current = 1")
    fun getCurrent(): Flow<History>

    @Query("SELECT * FROM history_table order by id desc limit 1")
    fun getLastHistory():Flow<History>

    @Query("SELECT * FROM history_table order by time desc limit 1")
    fun getMostRecent():Flow<History>

    @Query("SELECT * FROM history_table order by time desc")
    fun getHistoryOrder():Flow<List<History>>

    @Query("DELETE FROM history_table")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history :History)

    @Update
    suspend fun update(history: History)

    @Delete
    suspend fun delete(history: History)


}