package com.danDay.agsr.data

import androidx.room.*
import com.danDay.agsr.ui.goals.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
public interface GoalDao {

    fun getTask(query: String, sortOrder: SortOrder): Flow<List<Goal>> =
        when (sortOrder) {
            SortOrder.BY_DATE -> getTaskSortedByDate(query)
            SortOrder.BY_NAME -> getTaskSortedByName(query)
            SortOrder.BY_FAVORITE -> getTaskSortedByFav(query)
        }

    @Query("SELECT * FROM goal_table WHERE name LIKE '%' ||  :searchQuery || '%' ORDER BY favourite DESC, name")
    fun getTaskSortedByName(searchQuery: String): Flow<List<Goal>>

    @Query("SELECT * FROM goal_table WHERE name LIKE '%' ||  :searchQuery || '%' ORDER BY favourite DESC, created")
    fun getTaskSortedByDate(searchQuery: String): Flow<List<Goal>>


    @Query("SELECT * FROM goal_table WHERE name LIKE '%' ||  :searchQuery || '%' ORDER BY name DESC, favourite")
    fun getTaskSortedByFav(searchQuery: String): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal)

    @Update
    suspend fun update(goal: Goal)

    @Delete
    suspend fun delete(goal: Goal)
}