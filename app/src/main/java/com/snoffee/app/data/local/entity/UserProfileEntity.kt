package com.snoffee.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,

    @ColumnInfo(name = "height")
    val height: Double,

    @ColumnInfo(name = "weight")
    val weight: Double,

    @ColumnInfo(name = "sensitivity")
    val sensitivity: String, // "LOW", "NORMAL", "HIGH"

    @ColumnInfo(name = "target_sleep_time")
    val targetSleepTime: String,

    @ColumnInfo(name = "target_wake_time")
    val targetWakeTime: String,

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true
)