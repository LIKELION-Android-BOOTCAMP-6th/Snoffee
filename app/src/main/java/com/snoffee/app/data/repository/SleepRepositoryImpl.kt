package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.datasource.local.SleepLocalDataSource
import com.snoffee.app.data.mapper.SleepMapper
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.SleepSource
import com.snoffee.app.domain.repository.SleepRepository
import java.time.Instant
import java.time.ZoneId
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
            localSleepDataList + healthSleepDataList

        return deduplicateByDate(
            sleepDataList = mergedSleepDataList
        )
    }

    override suspend fun hasHealthPermission(): Boolean {
        return runCatching {
            healthDataSource.hasPermissions()
        }.getOrDefault(false)
    }

    private fun deduplicateByDate(
        sleepDataList: List<SleepData>
    ): List<SleepData> {
        return sleepDataList
            .groupBy { sleepData ->
                Instant.ofEpochMilli(sleepData.sleepEnd)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .map { (_, records) ->
                records.maxWith(
                    compareBy<SleepData> { sleepData ->
                        when (sleepData.source) {
                            SleepSource.MANUAL -> 2
                            SleepSource.SAMSUNG_HEALTH -> 1
                        }
                    }.thenBy { sleepData ->
                        sleepData.sleepEnd
                    }
                )
            }
            .sortedByDescending { sleepData ->
                sleepData.sleepEnd
            }
    }

    companion object {
        private const val DAYS_30_MILLIS =
            30L * 24L * 60L * 60L * 1000L
    }
}