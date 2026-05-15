package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject
import java.util.Calendar

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
    ): Pair<List<CaffeineRecord>, List<SleepData>> {
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
        return Pair(caffeineRecords, sleepData)
    }
    private fun getDateRange(
        period: ReportPeriod,
        baseTimeMillis: Long
    ): ReportDateRange {
        return when (period) {
            ReportPeriod.DAILY -> getDailyRange(baseTimeMillis)
            ReportPeriod.WEEKLY -> getWeeklyRange(baseTimeMillis)
            ReportPeriod.MONTHLY -> getMonthlyRange(baseTimeMillis)
            ReportPeriod.TREND -> getTrendRange(baseTimeMillis)
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
        return ReportDateRange(
            startTimeMillis = start,
            endTimeMillis = end
        )
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
        return ReportDateRange(
            startTimeMillis = start,
            endTimeMillis = end
        )
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
        return ReportDateRange(
            startTimeMillis = start,
            endTimeMillis = end
        )
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

            // 현재 달 포함 최근 3개월
            add(Calendar.MONTH, -2)
        }
        val start = calendar.timeInMillis
        val endCalendar = Calendar.getInstance().apply {
            timeInMillis = baseTimeMillis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // 다음 달 1일을 end로 사용
            add(Calendar.MONTH, 1)
        }
        val end = endCalendar.timeInMillis
        return ReportDateRange(
            startTimeMillis = start,
            endTimeMillis = end
        )
    }
    enum class ReportPeriod {
        DAILY,
        WEEKLY,
        MONTHLY,
        TREND
    }
    private data class ReportDateRange(
        val startTimeMillis: Long,
        val endTimeMillis: Long
    )
    companion object {
        private const val HOUR_MILLIS = 1000L * 60 * 60
        private const val ONE_DAY = 24L * HOUR_MILLIS
        private const val DAYS_7 = 7L * ONE_DAY
    }
}