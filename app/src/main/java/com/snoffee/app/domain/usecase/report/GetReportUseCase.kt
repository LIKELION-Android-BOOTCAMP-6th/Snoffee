package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.SleepRepository
import java.util.Calendar
import javax.inject.Inject

// 주간 카페인-수면 상관관계 리포트 조회 UseCase
// ReportViewModel에서 호출
// CaffeineRepository, SleepRepository를 통해 주간 데이터 조회
class GetReportUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,  // Hilt가 자동 주입
    private val sleepRepository: SleepRepository          // Hilt가 자동 주입
) {
    suspend operator fun invoke(
        period: ReportPeriod = ReportPeriod.WEEKLY,
        baseTimeMillis: Long = System.currentTimeMillis()
    ): ReportResult {
        val range = getDateRange(
            period = period,
            baseTimeMillis = baseTimeMillis
        )
        val caffeineRecords =
            caffeineRepository.getCaffeineRecordsByDateRange(
                startTimeMillis = range.startTimeMillis,
                endTimeMillis = range.endTimeMillis
            )
        val sleepData =
            sleepRepository.getSleepDataByDateRange(
                startTimeMillis = range.startTimeMillis,
                endTimeMillis = range.endTimeMillis
            )
        val caffeineGroupedByDay =
            caffeineRecords.groupBy { record ->
                getDayOfWeek(record.consumedAt)
            }
        val caffeineChartData =
            caffeineGroupedByDay.mapValues { entry ->
                entry.value.sumOf {
                    it.intakeCaffeine
                }
            }
        val sleepChartData =
            sleepData.groupBy { sleep ->
                getDayOfWeek(sleep.sleepEnd)
            }.mapValues { entry ->
                entry.value.sumOf { sleep ->
                    (sleep.sleepEnd - sleep.sleepStart
                            ) / HOUR_MILLIS.toDouble()
                }
            }
        return ReportResult(
            caffeineRecords = caffeineRecords,
            sleepData = sleepData,
            caffeineChartData = caffeineChartData,
            sleepChartData = sleepChartData,
            isEmpty = caffeineRecords.isEmpty() &&
                    sleepData.isEmpty()
        )
    }

    private fun getDayOfWeek(
        timeMillis: Long
    ): String {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timeMillis
        }
        return when (
            calendar.get(Calendar.DAY_OF_WEEK)
        ) {
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            Calendar.SUNDAY -> "일"
            else -> ""
        }
    }
    private fun getDateRange(
        period: ReportPeriod,
        baseTimeMillis: Long
    ): ReportDateRange {
        return when (period) {
            ReportPeriod.DAILY ->
                getDailyRange(baseTimeMillis)

            ReportPeriod.WEEKLY ->
                getWeeklyRange(baseTimeMillis)

            ReportPeriod.MONTHLY ->
                getMonthlyRange(baseTimeMillis)

            ReportPeriod.TREND ->
                getTrendRange(baseTimeMillis)
        }
    }
    private fun getDailyRange(
        baseTimeMillis: Long
    ): ReportDateRange {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        val end = start + ONE_DAY
        return ReportDateRange(start, end)
    }
    private fun getWeeklyRange(
        baseTimeMillis: Long
    ): ReportDateRange {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        val end = start + DAYS_7
        return ReportDateRange(start, end)
    }
    private fun getMonthlyRange(
        baseTimeMillis: Long
    ): ReportDateRange {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.MONTH, 1)
        val end = calendar.timeInMillis
        return ReportDateRange(start, end)
    }
    private fun getTrendRange(
        baseTimeMillis: Long
    ): ReportDateRange {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            add(Calendar.MONTH, -2)
        }
        val start = calendar.timeInMillis
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, 1)
        }
        val end = endCalendar.timeInMillis
        return ReportDateRange(start, end)
    }
    enum class ReportPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        TREND
    }
    data class ReportResult(
        val caffeineRecords: List<CaffeineRecord>,
        val sleepData: List<SleepData>,
        val caffeineChartData: Map<String, Double>,
        val sleepChartData: Map<String, Double>,
        val isEmpty: Boolean
    )
    private data class ReportDateRange(
        val startTimeMillis: Long,
        val endTimeMillis: Long
    )
    companion object {
        private const val HOUR_MILLIS =
            1000L * 60 * 60
        private const val ONE_DAY =
            24L * HOUR_MILLIS
        private const val DAYS_7 =
            7L * ONE_DAY
    }
}