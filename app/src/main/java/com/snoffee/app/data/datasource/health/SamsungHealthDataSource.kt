package com.snoffee.app.data.datasource.health

import com.snoffee.app.data.model.SleepDataDto

// 삼성헬스 데이터 접근 계약 (인터페이스)
// 실제 구현은 SamsungHealthDataSourceImpl에서
interface SamsungHealthDataSource {

    suspend fun saveSleepData(sleepData: SleepDataDto)

    suspend fun getLatestSleepData(): SleepDataDto?

    suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepDataDto>
}