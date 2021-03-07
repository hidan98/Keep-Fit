package com.danDay.agsr.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.logging.Filter
import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder { BY_NAME, BY_FAVORITE, BY_DATE }


data class FilterPreferences(val sortOrder: SortOrder, val goalId: Int, val historyId: Int)

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
            FilterPreferences(sortOrder, goalId, historyId)

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


    private object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val GOAL_ID = intPreferencesKey("goal_key")
        val HISTORY_ID = intPreferencesKey("history_key")
    }


}