package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.CaffeineLocalDataSource
import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.entity.CaffeineEntity
import com.snoffee.app.data.mapper.CaffeineMapper
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

// CaffeineRepository 인터페이스 구현체
// CaffeineLocalDataSource를 통해 Room DB에 접근
// CaffeineMapper를 통해 DTO ↔ Domain Model 변환
class CaffeineRepositoryImpl @Inject constructor(
    private val caffeineDao: CaffeineDao,
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
            .map { entities ->
                entities.map { entity ->
                    mapper.toDomain(entity)
                }
            }
    }

    override fun getCaffeineRecordsSince(sinceMillis: Long): Flow<List<CaffeineRecord>> {
        return getCaffeineRecordsByDateRange(
            startTimeMillis = sinceMillis,
            endTimeMillis = Long.MAX_VALUE
        )
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

    override suspend fun processAndInsertCaffeine(name: String, amount: Int, timestamp: Long) {
        val existingRecord = caffeineDao.getRecordByTimestamp(timestamp)

        if (existingRecord == null) {
            // 신규 데이터 생성 및 저장
            val newRecord = CaffeineEntity(
                drinkName = name,
                intakeCaffeine = amount.toDouble(),
                consumedAt = timestamp
            )
            caffeineDao.insertCaffeineRecord(newRecord)
        }
    }
}