package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.datasource.local.SleepLocalDataSource
import com.snoffee.app.data.mapper.SleepMapper
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

//SleepRepository 인터페이스 구현체
// SamsungHealthDataSource를 통해 삼성헬스 SDK에 접근
// SleepMapper를 통해 DTO ↔ Domain Model 변환
class SleepRepositoryImpl @Inject constructor(
    private val sleepDao: SleepDao,
    private val healthDataSource: SamsungHealthDataSource, // Hilt가 자동 주입
    private val mapper: SleepMapper,                        // Hilt가 자동 주입
    private val localDataSource: SleepLocalDataSource,
) : SleepRepository {
    override suspend fun saveSleepData(sleepData: SleepData) {
        val entity = mapper.toEntity(sleepData)
        localDataSource.insertSleepData(entity)
    }
    override suspend fun getLatestSleepData(): SleepData? {
        val healthSleepData = healthDataSource.getLatestSleepData()
        if (healthSleepData != null) {
            val domain = mapper.toDomain(healthSleepData)
            val entity = mapper.toEntity(domain)
            localDataSource.insertSleepData(entity)
            return domain
        }
        return localDataSource
            .getLatestSleepData()
            ?.let { entity ->
                mapper.toDomain(entity)
            }
    }
    override suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepData> {
        val healthSleepDataList =
            healthDataSource.getSleepDataByDateRange(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
        if (healthSleepDataList.isNotEmpty()) {
            val domainList =
                healthSleepDataList.map { dto -> mapper.toDomain(dto) }
            domainList.forEach { sleepData ->
                localDataSource.insertSleepData(
                    mapper.toEntity(sleepData)
                )
            }
            return domainList
        }
        return localDataSource
            .getSleepDataByDateRange(
                startTimeMillis = startTimeMillis,
                endTimeMillis = endTimeMillis
            )
            .map { entity -> mapper.toDomain(entity) }
    }
}