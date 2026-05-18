package com.snoffee.app.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.repository.SleepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val sleepRepository: SleepRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    fun loadReportData() {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val now = LocalDate.now()

            // 전체 통계 및 추이 분석을 위해 넉넉하게 최근 6개월 치의 데이터 가져옴
            val startMillis = now.minusMonths(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = now.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

            val rawSleepList = sleepRepository.getSleepDataByDateRange(startMillis, endMillis)
            // 유효 기록(점수가 0보다 큰 정상 등록 기록)만 필터링
            val sleepList = rawSleepList.filter { it.deepSleepRatio > 0 }

            if (sleepList.isEmpty()) {
                _uiState.update { it.copy(isDbEmpty = true) }
                return@launch
            }

            //일간(오늘) 데이터
            val todayRecord = sleepList.find {
                Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() == now
            }

            var todayHours = 0
            var todayMinutes = 0
            var todayTime = "0h 00m"

            if (todayRecord != null) {
                val durationMillis = todayRecord.sleepEnd - todayRecord.sleepStart
                val duration = Duration.ofMillis(durationMillis)

                todayHours = duration.toHours().toInt()
                todayMinutes = (duration.toMinutes() % 60).toInt()
                todayTime = String.format(Locale.KOREA, "%dh %02dm", todayHours, todayMinutes)
            }

            val todayStart = todayRecord?.let {
                Instant.ofEpochMilli(it.sleepStart).atZone(zoneId).toLocalTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))
            } ?: "--:--"

            //주간 데이터 가공 (최근 7일)
            val weeklyRecords = sleepList.filter {
                val recDate = Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
                recDate.isAfter(now.minusDays(7)) || recDate.isEqual(now)
            }
            var weeklyTotalMillis = 0L
            val weeklyScoresMap = mutableMapOf<LocalDate, Int>()
            weeklyRecords.forEach {
                weeklyTotalMillis += (it.sleepEnd - it.sleepStart)
                val recDate = Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
                weeklyScoresMap[recDate] = it.deepSleepRatio
            }
            val weeklyAvgTime = if (weeklyRecords.isNotEmpty()) {
                val avgDuration = Duration.ofMillis(weeklyTotalMillis / weeklyRecords.size)
                String.format(
                    Locale.KOREA,
                    "%dh %02dm",
                    avgDuration.toHours(),
                    avgDuration.toMinutes() % 60
                )
            } else "0h 00m"

            //월간 데이터 가공 (최근 30일)
            val monthlyRecords = sleepList.filter {
                val recDate = Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
                recDate.isAfter(now.minusDays(30)) || recDate.isEqual(now)
            }
            var monthlyTotalMillis = 0L
            monthlyRecords.forEach { monthlyTotalMillis += (it.sleepEnd - it.sleepStart) }
            val monthlyAvgTime = if (monthlyRecords.isNotEmpty()) {
                val avgDuration = Duration.ofMillis(monthlyTotalMillis / monthlyRecords.size)
                String.format(
                    Locale.KOREA,
                    "%dh %02dm",
                    avgDuration.toHours(),
                    avgDuration.toMinutes() % 60
                )
            } else "0h 00m"

            //전체 기간 통합 평균 및 BEST / WORST 월 분석
            var totalMillis = 0L
            sleepList.forEach { totalMillis += (it.sleepEnd - it.sleepStart) }
            val totalAvgTime = String.format(
                Locale.KOREA,
                "%dh %02dm",
                Duration.ofMillis(totalMillis / sleepList.size).toHours(),
                Duration.ofMillis(totalMillis / sleepList.size).toMinutes() % 60
            )

            // 월별 그룹화를 통해 가장 점수가 높은 월과 낮은 월을 추적
            val monthlyGroups = sleepList.groupBy {
                YearMonth.from(Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate())
            }
            var bestMonth: YearMonth? = null
            var worstMonth: YearMonth? = null
            var maxScore = -1
            var minScore = 999

            monthlyGroups.forEach { (yearMonth, records) ->
                val avgScore = records.map { it.deepSleepRatio }.average().toInt()
                if (avgScore > maxScore) {
                    maxScore = avgScore
                    bestMonth = yearMonth
                }
                if (avgScore < minScore) {
                    minScore = avgScore
                    worstMonth = yearMonth
                }
            }

            //UI State 최종 연결
            _uiState.update {
                it.copy(
                    todaySleepTime = todayTime,
                    todaySleepHours = todayHours,
                    todaySleepMinutes = todayMinutes,
                    todaySleepStart = todayStart,
                    hasTodayRecord = todayRecord != null,
                    weeklyAvgSleepTime = weeklyAvgTime,
                    weeklyScores = weeklyScoresMap,
                    monthlyAvgSleepTime = monthlyAvgTime,
                    totalAvgSleepTime = totalAvgTime,
                    bestMonthLabel = bestMonth?.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                            "M월"
                        )
                    ) ?: "데이터 없음",
                    bestMonthScore = if (maxScore != -1) maxScore else 0,
                    worstMonthLabel = worstMonth?.format(
                        java.time.format.DateTimeFormatter.ofPattern(
                            "M월"
                        )
                    ) ?: "데이터 없음",
                    worstMonthScore = if (minScore != 999) minScore else 0,
                    isDbEmpty = false
                )
            }
        }
    }
}