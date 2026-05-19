package com.snoffee.app.presentation.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.usecase.report.GetReportUseCase
import com.snoffee.app.domain.usecase.report.GetReportUseCase.ReportPeriod
import com.snoffee.app.domain.usecase.sleep.SaveSleepDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getReportUseCase: GetReportUseCase,
    private val saveSleepRecordUseCase: SaveSleepDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    private val _isSavingError = MutableStateFlow(false)
    val isSavingError = _isSavingError.asStateFlow()

    private val _isSaveSuccess = MutableStateFlow(false)
    val isSaveSuccess = _isSaveSuccess.asStateFlow()

    private var pendingRecord: SleepData? = null

    init {
        loadReportData()
    }

    fun saveSleepRecord(record: SleepData) {
        pendingRecord = record
        viewModelScope.launch {
            _isSavingError.value = false
            _isSaveSuccess.value = false

            val result = saveSleepRecordUseCase(record)

            if (result.isSuccess) {
                pendingRecord = null
                _isSaveSuccess.value = true
                loadReportData()
            } else {
                _isSavingError.value = true
            }
        }
    }

    fun retrySave() {
        pendingRecord?.let { saveSleepRecord(it) }
    }

    fun resetState() {
        _isSavingError.value = false
        _isSaveSuccess.value = false
        pendingRecord = null
    }

    fun resetErrorState() {
        _isSavingError.value = false
    }

    fun updatePeriodRange(start: LocalDate, end: LocalDate) {
        _uiState.update { it.copy(startDate = start, endDate = end) }
        calculatePeriodData(start, end)
    }
    fun loadReportData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val zoneId = ZoneId.systemDefault()
            val nowMillis = System.currentTimeMillis()

            val dailyDeferred = async { getReportUseCase(ReportPeriod.DAILY, nowMillis) }
            val weeklyDeferred = async { getReportUseCase(ReportPeriod.WEEKLY, nowMillis) }
            val monthlyDeferred = async { getReportUseCase(ReportPeriod.MONTHLY, nowMillis) }
            val trendDeferred = async { getReportUseCase(ReportPeriod.TREND, nowMillis) }

            val dailyResult = dailyDeferred.await()
            val weeklyResult = weeklyDeferred.await()
            val monthlyResult = monthlyDeferred.await()
            val trendResult = trendDeferred.await()

            if (dailyResult.isEmpty && weeklyResult.isEmpty && monthlyResult.isEmpty && trendResult.isEmpty) {
                _uiState.update { it.copy(isDbEmpty = true, isLoading = false) }
                return@launch
            }

            //일간 데이터
            val todayTotalCaffeine = dailyResult.caffeineRecords.sumOf { it.intakeCaffeine }.toInt()
            val todaySleepRecord = dailyResult.sleepData.firstOrNull()

            var todayHours = 0
            var todayMinutes = 0
            var todayTimeStr = "0h 00m"
            var todayStartStr = "--:--"

            todaySleepRecord?.let {
                val duration = Duration.ofMillis(it.sleepEnd - it.sleepStart)
                todayHours = duration.toHours().toInt()
                todayMinutes = (duration.toMinutes() % 60).toInt()
                todayTimeStr = String.format(Locale.KOREA, "%dh %02dm", todayHours, todayMinutes)
                todayStartStr = Instant.ofEpochMilli(it.sleepStart).atZone(zoneId).toLocalTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"))
            }

            //주간 데이터
            val weeklyAvgCaffeine = if (weeklyResult.caffeineRecords.isNotEmpty()) {
                (weeklyResult.caffeineRecords.sumOf { it.intakeCaffeine } / 7).toInt()
            } else 0

            val weeklyAvgSleepStr = if (weeklyResult.sleepData.isNotEmpty()) {
                val totalSleep = weeklyResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }
                val avgDuration = Duration.ofMillis(totalSleep / weeklyResult.sleepData.size)
                String.format(
                    Locale.KOREA,
                    "%dh %02dm",
                    avgDuration.toHours(),
                    avgDuration.toMinutes() % 60
                )
            } else "0h 00m"

            //월간 데이터
            val monthlyAvgCaffeine = if (monthlyResult.caffeineRecords.isNotEmpty()) {
                (monthlyResult.caffeineRecords.sumOf { it.intakeCaffeine } / 30).toInt()
            } else 0

            val monthlyAvgSleepStr = if (monthlyResult.sleepData.isNotEmpty()) {
                val totalSleep = monthlyResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }
                val avgDuration = Duration.ofMillis(totalSleep / monthlyResult.sleepData.size)
                String.format(
                    Locale.KOREA,
                    "%dh %02dm",
                    avgDuration.toHours(),
                    avgDuration.toMinutes() % 60
                )
            } else "0h 00m"

            //전체 기간 통합 평균 및 BEST / WORST 월 분석
            val totalAvgSleepStr = if (trendResult.sleepData.isNotEmpty()) {
                val totalSleep = trendResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }
                val avgDuration = Duration.ofMillis(totalSleep / trendResult.sleepData.size)
                String.format(
                    Locale.KOREA,
                    "%dh %02dm",
                    avgDuration.toHours(),
                    avgDuration.toMinutes() % 60
                )
            } else "0h 00m"

            val monthlyGroups = trendResult.sleepData.groupBy {
                java.time.YearMonth.from(Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate())
            }
            var bestMonth: java.time.YearMonth? = null
            var worstMonth: java.time.YearMonth? = null
            var maxScore = -1
            var minScore = 999

            monthlyGroups.forEach { (yearMonth, records) ->
                val avgScore = records.map { it.deepSleepRatio }.average().toInt()
                if (avgScore > maxScore) {
                    maxScore = avgScore; bestMonth = yearMonth
                }
                if (avgScore < minScore) {
                    minScore = avgScore; worstMonth = yearMonth
                }
            }

            val defaultStart = _uiState.value.startDate
            val defaultEnd = _uiState.value.endDate

            // 기간 탭에 맵핑할 데이터 필터링 수행
            val filteredCaffeine = trendResult.caffeineRecords.filter {
                val recDate = Instant.ofEpochMilli(it.consumedAt).atZone(zoneId).toLocalDate()
                (!recDate.isBefore(defaultStart)) && (!recDate.isAfter(defaultEnd))
            }
            val filteredSleep = trendResult.sleepData.filter {
                val recDate = Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                (!recDate.isBefore(defaultStart)) && (!recDate.isAfter(defaultEnd))
            }

            val daysBetween = (java.time.temporal.ChronoUnit.DAYS.between(
                defaultStart,
                defaultEnd
            ) + 1).coerceAtLeast(1)

            val periodTotalCaffeine = filteredCaffeine.sumOf { it.intakeCaffeine }.toInt()
            val periodAvgCaffeine = (periodTotalCaffeine / daysBetween).toInt()

            val periodTotalSleepMillis = filteredSleep.sumOf { it.sleepEnd - it.sleepStart }
            val periodTotalSleepStr = String.format(
                Locale.KOREA, "%dh %02dm",
                Duration.ofMillis(periodTotalSleepMillis).toHours(),
                Duration.ofMillis(periodTotalSleepMillis).toMinutes() % 60
            )

            val periodAvgSleepMillis =
                if (filteredSleep.isNotEmpty()) periodTotalSleepMillis / filteredSleep.size else 0L
            val periodAvgSleepStr = String.format(
                Locale.KOREA, "%dh %02dm",
                Duration.ofMillis(periodAvgSleepMillis).toHours(),
                Duration.ofMillis(periodAvgSleepMillis).toMinutes() % 60
            )

            _uiState.update {
                it.copy(
                    isDbEmpty = false,
                    isLoading = false,

                    periodTotalCaffeine = periodTotalCaffeine,
                    periodAvgCaffeine = periodAvgCaffeine,
                    periodTotalSleepTime = periodTotalSleepStr,
                    periodAvgSleepTime = periodAvgSleepStr,
                    periodCaffeineRecords = filteredCaffeine,

                    todaySleepTime = todayTimeStr,
                    todaySleepHours = todayHours,
                    todaySleepMinutes = todayMinutes,
                    todaySleepStart = todayStartStr,
                    hasTodayRecord = todaySleepRecord != null,
                    todayTotalCaffeine = todayTotalCaffeine,
                    todayCaffeineRecords = dailyResult.caffeineRecords,
                    weeklyAvgCaffeine = weeklyAvgCaffeine,
                    weeklyAvgSleepTime = weeklyAvgSleepStr,
                    weeklyCaffeineChartData = weeklyResult.caffeineChartData,
                    weeklySleepChartData = weeklyResult.sleepChartData,
                    monthlyAvgCaffeine = monthlyAvgCaffeine,
                    monthlyAvgSleepTime = monthlyAvgSleepStr,
                    totalAvgSleepTime = totalAvgSleepStr,
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
                    worstMonthScore = if (minScore != 999) minScore else 0
                )
            }
        }
    }

    private fun calculatePeriodData(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val endMillis = end.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
            val periodResult = getReportUseCase(GetReportUseCase.ReportPeriod.TREND, endMillis)

            val filteredCaffeine = periodResult.caffeineRecords.filter {
                val recDate = Instant.ofEpochMilli(it.consumedAt).atZone(zoneId).toLocalDate()
                (!recDate.isBefore(start)) && (!recDate.isAfter(end))
            }
            val filteredSleep = periodResult.sleepData.filter {
                val recDate = Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                (!recDate.isBefore(start)) && (!recDate.isAfter(end))
            }

            val daysBetween =
                (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).coerceAtLeast(1)

            val totalCaffeine = filteredCaffeine.sumOf { it.intakeCaffeine }.toInt()
            val avgCaffeine = (totalCaffeine / daysBetween).toInt()

            val totalSleepMillis = filteredSleep.sumOf { it.sleepEnd - it.sleepStart }
            val totalSleepStr = String.format(
                Locale.KOREA,
                "%dh %02dm",
                Duration.ofMillis(totalSleepMillis).toHours(),
                Duration.ofMillis(totalSleepMillis).toMinutes() % 60
            )

            val avgSleepMillis =
                if (filteredSleep.isNotEmpty()) totalSleepMillis / filteredSleep.size else 0L
            val avgSleepStr = String.format(
                Locale.KOREA,
                "%dh %02dm",
                Duration.ofMillis(avgSleepMillis).toHours(),
                Duration.ofMillis(avgSleepMillis).toMinutes() % 60
            )

            _uiState.update {
                it.copy(
                    periodTotalCaffeine = totalCaffeine,
                    periodAvgCaffeine = avgCaffeine,
                    periodTotalSleepTime = totalSleepStr,
                    periodAvgSleepTime = avgSleepStr,
                    periodCaffeineRecords = filteredCaffeine
                )
            }
        }
    }
}