package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.CaffeineRecord

// 카페인 섭취 기록 저장/조회/삭제 계약
// 실제 구현은 CaffeineRepositoryImpl
interface CaffeineRepository {
    suspend fun saveCaffeineRecord(record: CaffeineRecord)  // 카페인 섭취 기록 저장
    suspend fun getTodayCaffeineRecords(): List<CaffeineRecord>  // 오늘 섭취 목록 조회

    //게이지 계산용 섭취 목록 조회
    suspend fun getCaffeineRecordsSince(sinceMillis: Long): List<CaffeineRecord>
    suspend fun deleteCaffeineRecord(id: Long)              // 카페인 섭취 기록 삭제
    suspend fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<CaffeineRecord>
}