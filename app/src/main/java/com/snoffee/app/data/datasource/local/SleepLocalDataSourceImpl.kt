package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.dao.SleepDao
import com.snoffee.app.data.local.entity.SleepEntity
import javax.inject.Inject

class SleepLocalDataSourceImpl @Inject constructor(
    private val sleepDao: SleepDao
) : SleepLocalDataSource {
    override suspend fun insertSleepData(sleepData: SleepEntity) {
        sleepDao.insertSleepData(sleepData)
    }

    override suspend fun getLatestSleepData(): SleepEntity? {
        return sleepDao.getLatestSleepData()
    }

    override suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepEntity> {
        return sleepDao.getSleepDataByDateRange(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis
        )
    }
}