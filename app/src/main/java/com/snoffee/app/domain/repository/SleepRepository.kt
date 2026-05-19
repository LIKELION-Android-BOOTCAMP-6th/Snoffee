package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.SleepData

// 수면 데이터 저장/조회 계약
// 실제 구현은 SleepRepositoryImpl
interface SleepRepository {
    suspend fun saveSleepData(sleepData: SleepData): Result<Unit>  // 수면 데이터 저장
    suspend fun getLatestSleepData(): SleepData?            // 최근 수면 데이터 조회 (없을 수 있음)
    suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepData>
}