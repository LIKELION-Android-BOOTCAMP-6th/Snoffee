package com.snoffee.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snoffee.app.data.local.entity.SleepEntity

@Dao
interface SleepDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepData(sleepData: SleepEntity)

    @Query(
        """
        SELECT * FROM sleep_data
        ORDER BY sleep_end DESC
        LIMIT 1
    """
    )
    suspend fun getLatestSleepData(): SleepEntity?

    @Query(
        """
        SELECT * FROM sleep_data
        WHERE sleep_end BETWEEN :startTimeMillis AND :endTimeMillis
        ORDER BY sleep_end DESC
    """
    )
    suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepEntity>
}