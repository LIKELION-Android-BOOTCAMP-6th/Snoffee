package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.CaffeineLocalDataSource
import com.snoffee.app.data.mapper.CaffeineMapper
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

// CaffeineRepository 인터페이스 구현체
// CaffeineLocalDataSource를 통해 Room DB에 접근
// CaffeineMapper를 통해 DTO ↔ Domain Model 변환
class CaffeineRepositoryImpl @Inject constructor(
    private val localDataSource: CaffeineLocalDataSource,  // Hilt가 자동 주입
    private val mapper: CaffeineMapper                     // Hilt가 자동 주입
) : CaffeineRepository {

    override suspend fun saveCaffeineRecord(record: CaffeineRecord) {
        val entity = mapper.toEntity(record)
        localDataSource.insertCaffeineRecord(entity)
    }

    override fun getTodayCaffeineRecords(): Flow<List<CaffeineRecord>> {
        val startOfDay = getStartOfDay()
        val endOfDay = startOfDay + ONE_DAY

        return localDataSource
            .getTodayRecords(startOfDay, endOfDay)
            .map { entities ->              // first 제거 후 map으로 변경
                entities.map { entity ->
                    mapper.toDomain(entity)
                }
            }
    }

    override suspend fun getCaffeineRecordsSince(sinceMillis: Long): List<CaffeineRecord> {
        return getCaffeineRecordsByDateRange(
            startTimeMillis = sinceMillis,
            endTimeMillis = Long.MAX_VALUE
        ).first()
    }

    override suspend fun deleteCaffeineRecord(id: Long) {
        localDataSource.deleteCaffeineRecord(id)
    }

    override fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Flow<List<CaffeineRecord>> {
        return localDataSource
            .getCaffeineRecordsByDateRange(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
            .map { entities ->
                entities.map { mapper.toDomain(it) }
            }
    }

    override suspend fun editCaffeineRecord(record: CaffeineRecord) {
        val entity = mapper.toEntity(record)
        localDataSource.editCaffeineRecord(entity)
    }

    private fun getStartOfDay(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return calendar.timeInMillis
    }

    companion object {
        private const val ONE_DAY = 24L * 60 * 60 * 1000
    }
}