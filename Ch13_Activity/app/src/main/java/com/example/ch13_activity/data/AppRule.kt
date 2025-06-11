package com.example.ch13_activity.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_rules")
data class AppRule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val appName: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val rules: String,
    @ColumnInfo(name = "repeat_type") val repeatType: String
)

