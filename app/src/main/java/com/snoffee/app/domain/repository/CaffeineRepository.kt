package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.CaffeineRecord
import kotlinx.coroutines.flow.Flow

// 카페인 섭취 기록 저장/조회/삭제 계약
// 실제 구현은 CaffeineRepositoryImpl
interface CaffeineRepository {
    // 카페인 섭취 기록 저장
    suspend fun saveCaffeineRecord(record: CaffeineRecord)

    // 오늘 섭취 목록 조회
    fun getTodayCaffeineRecords(): Flow<List<CaffeineRecord>>

    //게이지 계산용 섭취 목록 조회
    suspend fun getCaffeineRecordsSince(sinceMillis: Long): List<CaffeineRecord>

    // 카페인 섭취 기록 삭제
    suspend fun deleteCaffeineRecord(id: Long)

    fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): Flow<List<CaffeineRecord>>

    // 카페인 섭취 기록 수정
    suspend fun editCaffeineRecord(record: CaffeineRecord)
}