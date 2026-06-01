package com.snoffee.app.data.datasource.health

import android.content.Context
import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.snoffee.app.data.model.SleepDataDto
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

// SamsungHealthDataSource 구현체
// 삼성헬스 SDK 실제 연동 구현
// 수면/심박 데이터 수집 로직 포함
class SamsungHealthDataSourceImpl @Inject constructor(
    @ApplicationContext
    private val context: Context
) : SamsungHealthDataSource {
    private val healthConnectClient by lazy {
        HealthConnectClient.getOrCreate(context)
    }
    val permissions = setOf(
        HealthPermission.getReadPermission(
            SleepSessionRecord::class
        )
    )
    private fun calculateSleepScore(
        sleepStart: Long,
        sleepEnd: Long
    ): Int {
        val sleepHours =
            (sleepEnd - sleepStart) / (1000.0 * 60.0 * 60.0)

        return when {
            sleepHours >= 7.0 && sleepHours <= 9.0 -> 80
            sleepHours >= 6.0 && sleepHours < 7.0 -> 65
            sleepHours > 9.0 && sleepHours <= 10.0 -> 70
            sleepHours >= 5.0 && sleepHours < 6.0 -> 50
            else -> 40
        }
    }
    override suspend fun saveSleepData(
        sleepData: SleepDataDto
    ) {
        // 현재는 읽기 중심이라 미구현
        // TODO:
        // 필요 시 Health Connect 쓰기 구현
    }
    override suspend fun getLatestSleepData(): SleepDataDto? {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000
        return getSleepDataByDateRange(
            startTimeMillis = sevenDaysAgo,
            endTimeMillis = now
        ).maxByOrNull {
            it.sleepEnd
        }
    }
    override suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepDataDto> {
        if (!hasPermissions()) {
            return emptyList()
        }

        val bufferedStartTimeMillis =
            startTimeMillis - 12L * 60L * 60L * 1000L

        val bufferedEndTimeMillis =
            endTimeMillis + 12L * 60L * 60L * 1000L

        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(
                    Instant.ofEpochMilli(bufferedStartTimeMillis),
                    Instant.ofEpochMilli(bufferedEndTimeMillis)
                )
            )
        )

        Log.d("HealthConnect", "sleep records size = ${response.records.size}")

        return response.records.map { record ->
            val sleepStart = record.startTime.toEpochMilli()
            val sleepEnd = record.endTime.toEpochMilli()

            SleepDataDto(
                date = LocalDateTime.ofInstant(record.endTime, ZoneId.systemDefault()),
                sleepStart = sleepStart,
                sleepEnd = sleepEnd,
                deepSleepRatio = calculateSleepScore(sleepStart, sleepEnd)
            )
        }
    }

    override suspend fun hasPermissions(): Boolean {

        val permissions = setOf(
            HealthPermission.getReadPermission(
                SleepSessionRecord::class
            )
        )

        val granted =
            healthConnectClient.permissionController
                .getGrantedPermissions()

        return granted.containsAll(permissions)
    }
}
