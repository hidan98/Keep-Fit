package com.danDay.agsr.data

import android.content.Context
import androidx.datastore.createDataStore
import androidx.datastore.preferences.createDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalHistoryStore @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore(name = "history_goal")
}