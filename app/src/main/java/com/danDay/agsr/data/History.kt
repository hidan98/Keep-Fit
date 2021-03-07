package com.danDay.agsr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.reflect.Array.get
import java.text.DateFormat

@Entity(tableName = "history_table")
data class History(
    val time : Long = System.currentTimeMillis(),
    val steps : Long = 0,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val current: Boolean
) {
    val createDateFormat : String
        get()=DateFormat.getDateInstance(DateFormat.SHORT).format(time)
}