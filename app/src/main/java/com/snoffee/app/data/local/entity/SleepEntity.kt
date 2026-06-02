package com.snoffee.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sleep_data",
    indices = [
        Index(
            value = ["sleep_start", "sleep_end", "source"],
            unique = true
        )
    ]
)
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
    val deepSleepRatio: Int,

    @ColumnInfo(name = "source")
    val source: String = "MANUAL"
)