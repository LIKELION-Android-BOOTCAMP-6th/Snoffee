package com.snoffee.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snoffee.app.data.local.entity.CaffeineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaffeineDao {

    // 카페인 기록 저장
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCaffeineRecord(record: CaffeineEntity)

    // 카페인 기록 삭제
    @Query("DELETE FROM caffeine_record WHERE id = :id")
    suspend fun deleteCaffeineRecord(id: Long)

    // 오늘 기록 조회 (홈 화면 자동 반영용)
    @Query(
        "SELECT * FROM caffeine_record " +
                "WHERE consumed_at " +
                "BETWEEN :startOfDay AND :endOfDay " +
                "ORDER BY consumed_at DESC"
    )

    fun getTodayRecords(startOfDay: Long, endOfDay: Long): Flow<List<CaffeineEntity>>

    @Query(
        "SELECT * FROM caffeine_record " +
                "WHERE consumed_at BETWEEN :startTimeMillis AND :endTimeMillis " +
                "ORDER BY consumed_at DESC"
    )
    suspend fun getCaffeineRecordsByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<CaffeineEntity>
}