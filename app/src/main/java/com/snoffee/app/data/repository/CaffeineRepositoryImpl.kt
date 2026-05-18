package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.CaffeineLocalDataSource
import com.snoffee.app.data.mapper.CaffeineMapper
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import kotlinx.coroutines.flow.first
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
        // TODO: Domain Model → DTO 변환 후 저장
        val entity = mapper.toEntity(record)
        localDataSource.insertCaffeineRecord(entity)
    }

    override suspend fun getTodayCaffeineRecords(): List<CaffeineRecord> {
        val startOfDay = getStartOfDay()
        val endOfDay = startOfDay + ONE_DAY

        return localDataSource
            .getTodayRecords(startOfDay, endOfDay)
            .first()
            .map { entity ->
                mapper.toDomain(entity)
            }
    }

    override suspend fun deleteCaffeineRecord(id: Long) {
        localDataSource.deleteCaffeineRecord(id)
    }

    override suspend fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<CaffeineRecord> {
        return localDataSource
            .getCaffeineRecordsByDateRange(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
            .map { entity ->
                mapper.toDomain(entity)
            }
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