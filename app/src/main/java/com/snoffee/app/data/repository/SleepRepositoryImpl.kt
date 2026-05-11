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
        // TODO: Domain Model → DTO 변환 후 저장
    }

    override suspend fun getLatestSleepData(): SleepData? {
        // TODO: 삼성헬스에서 최근 수면 데이터 조회 후 Domain Model로 변환해서 반환
        return null
    }
}