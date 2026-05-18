package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.mapper.SleepMapper
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

//SleepRepository 인터페이스 구현체
// SamsungHealthDataSource를 통해 삼성헬스 SDK에 접근
// SleepMapper를 통해 DTO ↔ Domain Model 변환
class SleepRepositoryImpl @Inject constructor(
    private val healthDataSource: SamsungHealthDataSource, // Hilt가 자동 주입
    private val mapper: SleepMapper                        // Hilt가 자동 주입
) : SleepRepository {
    override suspend fun saveSleepData(sleepData: SleepData) {
        val dto = mapper.toDto(sleepData)
        healthDataSource.saveSleepData(dto)
    }
    override suspend fun getLatestSleepData(): SleepData? {
        return healthDataSource
            .getLatestSleepData()
            ?.let { dto ->
                mapper.toDomain(dto)
            }
    }

    override suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepData> {
        return healthDataSource
            .getSleepDataByDateRange(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
            .map { dto ->
                mapper.toDomain(dto)
            }
    }
}