package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.entity.SleepEntity

interface SleepLocalDataSource {
    suspend fun insertSleepData(sleepData: SleepEntity)
    suspend fun getLatestSleepData(): SleepEntity?
    suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepEntity>
}