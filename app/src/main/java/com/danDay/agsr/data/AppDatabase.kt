package com.danDay.agsr.data

import android.util.Log
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.danDay.agsr.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Goal::class, History::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDoa(): GoalDao
    abstract fun historyDoa(): HistoryDao

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            Log.d("myLog", "created")
            val goalDao = database.get().goalDoa()
            val historyDoa = database.get().historyDoa()

            applicationScope.launch {
                Log.d("myLog", "inserting")
                goalDao.insert(Goal("A", 10000))
                goalDao.insert(Goal("B", 32))
                goalDao.insert(Goal("C", 2354))
                goalDao.insert(Goal("D", 10))
                goalDao.insert(Goal("E", 14353450))





            }

        }
    }
}