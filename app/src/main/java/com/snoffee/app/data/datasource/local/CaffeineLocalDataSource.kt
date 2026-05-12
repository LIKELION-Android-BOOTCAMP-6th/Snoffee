package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.entity.CaffeineEntity
import kotlinx.coroutines.flow.Flow

// 카페인 로컬 데이터 접근 계약 (인터페이스)
// 실제 구현은 CaffeineLocalDataSourceImpl
interface CaffeineLocalDataSource {
    suspend fun insertCaffeineRecord(entity: CaffeineEntity)        // 카페인 기록 저장
    fun getTodayRecords(startOfDay: Long, endOfDay: Long): Flow<List<CaffeineEntity>>  // 오늘 기록 조회
    suspend fun deleteCaffeineRecord(id: Long)                      // 카페인 기록 삭제
}