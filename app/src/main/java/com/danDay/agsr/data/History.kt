package com.danDay.agsr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.reflect.Array.get
import java.text.DateFormat

@Entity(tableName = "history_table")
data class History(
    val time : Long = System.currentTimeMillis(),
    val steps : Int,
    @PrimaryKey(autoGenerate = true) val id: Int = 0
) {
    val createDateFormat : String
        get()=DateFormat.getDateInstance().format(time)
}