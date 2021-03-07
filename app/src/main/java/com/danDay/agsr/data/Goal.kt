package com.danDay.agsr.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.text.DateFormat

@Entity(tableName = "goal_table")
@Parcelize
data class Goal(
    val name:String,
    val steps: Int,
    val favourite: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val created: Long = System.currentTimeMillis(),
    val active: Boolean = false
) : Parcelable{

    val createDateFormatted: String
        get() = DateFormat.getDateInstance(DateFormat.SHORT).format(created)
}