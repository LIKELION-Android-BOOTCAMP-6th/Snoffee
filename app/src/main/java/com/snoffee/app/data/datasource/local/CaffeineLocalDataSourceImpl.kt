package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.entity.CaffeineEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// CaffeineLocalDataSource 구현체
class CaffeineLocalDataSourceImpl @Inject constructor(
    private val dao: CaffeineDao
) : CaffeineLocalDataSource {
    override suspend fun insertCaffeineRecord(entity: CaffeineEntity) {
        dao.insertCaffeineRecord(entity)
    }

    override fun getTodayRecords(startOfDay: Long, endOfDay: Long): Flow<List<CaffeineEntity>> {
        return dao.getTodayRecords(startOfDay, endOfDay)
    }

    override suspend fun deleteCaffeineRecord(id: Long) {
        dao.deleteCaffeineRecord(id)
    }
    override suspend fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<CaffeineEntity> {
        return dao.getCaffeineRecordsByDateRange(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis
        )
    }
}