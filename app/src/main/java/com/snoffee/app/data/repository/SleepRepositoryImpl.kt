package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.local.dao.SleepDao
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
    private val mapper: SleepMapper                        // Hilt가 자동 주입
) : SleepRepository {
    override suspend fun saveSleepData(sleepData: SleepData): Result<Unit> = runCatching {
        if (sleepData.source == "manual") {
            //수동 입력 -> Room DB에 직접 저장
            val entity = mapper.toEntity(sleepData)
            sleepDao.insertSleepData(entity)
        } else {
            //헬스 데이터 ->️ 기존 파트원 로직대로  저장
            val dto = mapper.toDto(sleepData)
            healthDataSource.saveSleepData(dto)
        }
    }
    override suspend fun getLatestSleepData(): SleepData? {
        // 로컬 Room DB에 최신 데이터가 있다면 우선 반환, 없다면 삼성헬스 조회
        val localLatest = sleepDao.getLatestSleepData()?.let { mapper.toDomain(it) }
        if (localLatest != null) return localLatest

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
        //두 데이터 소스의 결과를 합쳐서 제공
        val localData = sleepDao.getSleepDataByDateRange(startTimeMillis, endTimeMillis)
            .map { mapper.toDomain(it) }

        val healthData = runCatching {
            healthDataSource.getSleepDataByDateRange(startTimeMillis, endTimeMillis)
                .map { mapper.toDomain(it) }
        }.getOrDefault(emptyList())

        // 두 리스트를 합치고 날짜 역순정렬해서 반환
        return (localData + healthData).sortedByDescending { it.sleepEnd }
    }
}