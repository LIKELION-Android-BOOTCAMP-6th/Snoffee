package com.snoffee.app.data.datasource.health

import android.content.Context
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
        val response =
            healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(
                        Instant.ofEpochMilli(startTimeMillis),
                        Instant.ofEpochMilli(endTimeMillis)
                    )
                )
            )
        return response.records.map { record ->
            SleepDataDto(
                date = LocalDateTime.ofInstant(
                    record.startTime,
                    ZoneId.systemDefault()
                ),
                sleepStart = record.startTime.toEpochMilli(),
                sleepEnd = record.endTime.toEpochMilli(),
                deepSleepRatio = 0
            )
        }
    }

    suspend fun hasPermissions(): Boolean {
        return healthConnectClient
            .permissionController
            .getGrantedPermissions()
            .containsAll(permissions)
    }
}