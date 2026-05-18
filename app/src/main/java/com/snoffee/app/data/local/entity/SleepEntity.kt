package com.snoffee.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_data")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "sleep_start")
    val sleepStart: Long,

    @ColumnInfo(name = "sleep_end")
    val sleepEnd: Long,

    @ColumnInfo(name = "deep_sleep_ratio")
    val deepSleepRatio: Double,

    @ColumnInfo(name = "source")
    val source: String = "manual"
)