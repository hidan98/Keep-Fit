package com.danDay.agsr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.logging.Filter
import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder { BY_NAME, BY_FAVORITE, BY_DATE }


data class FilterPreferences(val sortOrder: SortOrder, val goalId: Int, val historyId: Int, val stepStore :Long)

@Singleton
class PreferencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore(name = "test")
    val preferencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val sortOrder = SortOrder.valueOf(
                preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val goalId = preferences[PreferencesKeys.GOAL_ID]?:1
            val historyId = preferences[PreferencesKeys.HISTORY_ID]?:1
            val step = preferences[PreferencesKeys.StepStore]?:0
            FilterPreferences(sortOrder, goalId, historyId, step)

        }

    suspend fun updateSortOrder(sortOrder: SortOrder) {
        dataStore.edit { prefernces ->
            prefernces[PreferencesKeys.SORT_ORDER] = sortOrder.name
        }

    }

    suspend fun updateGalID(id: Int) {
        dataStore.edit { prefernces ->
            prefernces[PreferencesKeys.GOAL_ID] = id
        }
    }
    suspend fun updateHistoryID(id: Int) {
        dataStore.edit { prefernces ->
            prefernces[PreferencesKeys.HISTORY_ID] = id
        }
    }
    suspend fun updateStepCheck(steps:Long){
        dataStore.edit {prefernces->
            prefernces[PreferencesKeys.StepStore]=steps
        }
    }


    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val GOAL_ID = intPreferencesKey("goal_key")
        val HISTORY_ID = intPreferencesKey("history_key")
        val StepStore = longPreferencesKey("StepStore_key")

    }


}