package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.datasource.local.SleepLocalDataSource
import com.snoffee.app.data.mapper.SleepMapper
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.SleepSource
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

class SleepRepositoryImpl @Inject constructor(
    private val healthDataSource: SamsungHealthDataSource,
    private val mapper: SleepMapper,
    private val localDataSource: SleepLocalDataSource,
) : SleepRepository {

    override suspend fun saveSleepData(
        sleepData: SleepData
    ): Result<Unit> {
        return runCatching {
            val entity = mapper.toEntity(
                sleepData.copy(
                    source = SleepSource.MANUAL
                )
            )

            localDataSource.insertSleepData(entity)
        }
    }

    override suspend fun deleteSleepData(
        sleepData: SleepData
    ): Result<Unit> {
        return runCatching {
            val entity = mapper.toEntity(sleepData)
            localDataSource.deleteSleepData(entity)
        }
    }

    override suspend fun getLatestSleepData(): SleepData? {
        val endTimeMillis = System.currentTimeMillis()
        val startTimeMillis = endTimeMillis - DAYS_30_MILLIS

        return getSleepDataByDateRange(
            startTimeMillis = startTimeMillis,
            endTimeMillis = endTimeMillis
        ).maxByOrNull { sleepData ->
            sleepData.sleepEnd
        }
    }

    override suspend fun getSleepDataByDateRange(
        startTimeMillis: Long,
        endTimeMillis: Long
    ): List<SleepData> {
        val localSleepDataList =
            localDataSource
                .getSleepDataByDateRange(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis
                )
                .map { entity ->
                    mapper.toDomain(entity)
                }

        val healthSleepDataList =
            runCatching {
                healthDataSource.getSleepDataByDateRange(
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis
                ).map { dto ->
                    mapper.toDomain(dto)
                }
            }.getOrDefault(emptyList())

        healthSleepDataList.forEach { sleepData ->
            localDataSource.insertSleepData(
                mapper.toEntity(sleepData)
            )
        }

        val mergedSleepDataList =
            (localSleepDataList + healthSleepDataList).distinctBy { sleepData ->
                // 고유 식별자인 시작 시간, 종료 시간, 데이터 출처 조합으로 유니크 키 생성
                "${sleepData.sleepStart}_${sleepData.sleepEnd}_${sleepData.source}"
            }

        return resolveSourceConflictByDate(
            sleepDataList = mergedSleepDataList
        )
    }

    override suspend fun hasHealthPermission(): Boolean {
        return runCatching {
            healthDataSource.hasPermissions()
        }.getOrDefault(false)
    }

    private fun resolveSourceConflictByDate(
        sleepDataList: List<SleepData>
    ): List<SleepData> {
        val manualRecords = sleepDataList.filter { it.source == SleepSource.MANUAL }
        val healthRecords = sleepDataList.filter { it.source == SleepSource.SAMSUNG_HEALTH }

        val filteredHealthRecords = healthRecords.filter { health ->
            manualRecords.none { manual ->
                // 시간 매칭 중복 검증 (서로의 영역을 침범하는지 체크)
                health.sleepStart < manual.sleepEnd && health.sleepEnd > manual.sleepStart
            }
        }

        return (manualRecords + filteredHealthRecords).sortedBy { it.sleepStart }
    }

    companion object {
        private const val DAYS_30_MILLIS =
            30L * 24L * 60L * 60L * 1000L
    }
}