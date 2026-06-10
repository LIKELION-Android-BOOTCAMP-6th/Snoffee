package com.snoffee.app.presentation.report

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.core.caffeine.CaffeineCalculator
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.usecase.gemini.GetMonthlyInsightUseCase
import com.snoffee.app.domain.usecase.gemini.GetPeriodInsightUseCase
import com.snoffee.app.domain.usecase.gemini.GetTrendInsightUseCase
import com.snoffee.app.domain.usecase.gemini.GetWeeklyInsightUseCase
import com.snoffee.app.domain.usecase.report.GetReportUseCase
import com.snoffee.app.domain.usecase.sleep.SaveSleepDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val getReportUseCase: GetReportUseCase,
    private val saveSleepRecordUseCase: SaveSleepDataUseCase,
    private val sleepRepository: SleepRepository,
    private val getWeeklyInsightUseCase: GetWeeklyInsightUseCase,
    private val getTrendInsightUseCase: GetTrendInsightUseCase,
    private val getMonthlyInsightUseCase: GetMonthlyInsightUseCase,
    private val getPeriodInsightUseCase: GetPeriodInsightUseCase,
    private val userProfileRepository: UserProfileRepository,
    private val caffeineCalculator: CaffeineCalculator
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()

    private val _isSavingError = MutableStateFlow(false)
    val isSavingError = _isSavingError.asStateFlow()

    private val _isSaveSuccess = MutableStateFlow(false)
    val isSaveSuccess = _isSaveSuccess.asStateFlow()

    private var pendingRecord: SleepData? = null
    private var cachedLateCaffeineCount: Int = 0
    private var cachedBedtimeResidualCaffeineMg: Int = 0
    private var cachedMonthlySleepTrend: Map<String, Double> = emptyMap()
    private var hasLoadedReportInsights = false

    //자정 감지 브로드캐스트 리시버
    private val dateChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_DATE_CHANGED) {
                _uiState.update { currentState ->
                    // 자정이 지나면 '기간 탭'의 기본 기간도 자동으로 오늘 기준으로 한 칸 미뤄 밀림 방지
                    currentState.copy(
                        startDate = LocalDate.now().minusDays(6),
                        endDate = LocalDate.now()
                    )
                }
                // 전체 일간/주간/월간 비동기 통계 대시보드 리로드 수행
                loadReportData()
            }
        }
    }

    init {
        loadReportData()
        val filter = IntentFilter(Intent.ACTION_DATE_CHANGED)
        context.registerReceiver(dateChangedReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(dateChangedReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
                hasLoadedReportInsights = false
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
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isError = false,
                    errorMessage = null,
                    isPeriodInsightLoading = true
                )
            }
            val zoneId = ZoneId.systemDefault()
            val nowMillis = System.currentTimeMillis()


            try {
                val dailyDeferred =
                    async { getReportUseCase(GetReportUseCase.ReportPeriod.DAILY, nowMillis) }
                val weeklyDeferred =
                    async { getReportUseCase(GetReportUseCase.ReportPeriod.WEEKLY, nowMillis) }
                val monthlyDeferred =
                    async { getReportUseCase(GetReportUseCase.ReportPeriod.MONTHLY, nowMillis) }
                val trendDeferred =
                    async { getReportUseCase(GetReportUseCase.ReportPeriod.TREND, nowMillis) }

                val dailyResult = dailyDeferred.await()
                val weeklyResult = weeklyDeferred.await()
                val monthlyResult = monthlyDeferred.await()
                val trendResult = trendDeferred.await()

                if (dailyResult.isEmpty && weeklyResult.isEmpty && monthlyResult.isEmpty && trendResult.isEmpty) {
                    _uiState.update {
                        it.copy(
                            isDbEmpty = true,
                            isLoading = false,
                            totalSleepDaysCount = 0
                        )
                    }
                    return@launch
                }
                val totalSleepDaysCount = trendResult.sleepData.groupBy {
                    Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                }.size

                //일간 데이터
                val todayTotalCaffeine =
                    dailyResult.caffeineRecords.sumOf { it.intakeCaffeine }.toInt()
                val todayLocalDate = dailyResult.caffeineRecords.firstOrNull()?.let {
                    Instant.ofEpochMilli(it.consumedAt).atZone(zoneId).toLocalDate()
                } ?: Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

                val minAllowStart =
                    todayLocalDate.minusDays(1).atTime(20, 0).atZone(zoneId).toInstant()
                        .toEpochMilli()
                val maxAllowEnd =
                    todayLocalDate.atTime(14, 0).atZone(zoneId).toInstant().toEpochMilli()

                val todaySleepRecords = trendResult.sleepData.filter {
                    it.sleepStart >= minAllowStart && it.sleepEnd <= maxAllowEnd
                }

                var todayHours = 0
                var todayMinutes = 0
                var todayTimeStr = "0h 00m"
                var todayStartStr = "--:--"
                val hasTodayRecord = todaySleepRecords.isNotEmpty()

                if (hasTodayRecord) {
                    val totalTodayMillis = todaySleepRecords.sumOf { it.sleepEnd - it.sleepStart }
                    val duration = Duration.ofMillis(totalTodayMillis)

                    todayHours = duration.toHours().toInt()
                    todayMinutes = (duration.toMinutes() % 60).toInt()
                    todayTimeStr =
                        String.format(Locale.KOREA, "%dh %02dm", todayHours, todayMinutes)

                    val earliestSleepStart = todaySleepRecords.minOf { it.sleepStart }
                    todayStartStr =
                        Instant.ofEpochMilli(earliestSleepStart).atZone(zoneId).toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH:mm"))
                }

                //주간 데이터
                val weeklyAvgCaffeine = if (weeklyResult.caffeineRecords.isNotEmpty()) {
                    (weeklyResult.caffeineRecords.sumOf { it.intakeCaffeine } / 7).toInt()
                } else 0

                val weeklyAvgSleepStr = if (weeklyResult.sleepData.isNotEmpty()) {
                    val weeklySleepByDate = weeklyResult.sleepData.groupBy {
                        Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                    }
                    val totalWeeklySleepMillis =
                        weeklyResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }

                    val avgDuration =
                        Duration.ofMillis(totalWeeklySleepMillis / 7)
                    String.format(
                        Locale.KOREA,
                        "%dh %02dm",
                        avgDuration.toHours(),
                        avgDuration.toMinutes() % 60
                    )
                } else "0h 00m"

                val beforeSleepHours = 6

                val lateCaffeineCount =
                    weeklyResult.caffeineRecords.count { caffeine ->
                        weeklyResult.sleepData.any { sleep ->
                            caffeine.consumedAt in
                                    (sleep.sleepStart - beforeSleepHours * 60L * 60L * 1000L)..sleep.sleepStart
                        }
                    }

                val bedtimeResidualCaffeineMg =
                    calculateBedtimeResidualCaffeineMg(
                        caffeineRecords = weeklyResult.caffeineRecords,
                        sleepData = weeklyResult.sleepData
                    )
                //월간 데이터
                val currentLocalDate = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
                val daysInCurrentMonth = YearMonth.from(currentLocalDate).lengthOfMonth()
                val monthlyAvgCaffeine = if (monthlyResult.caffeineRecords.isNotEmpty()) {
                    (monthlyResult.caffeineRecords.sumOf { it.intakeCaffeine } / daysInCurrentMonth).toInt()
                } else 0
                val monthlyAvgSleepStr = if (monthlyResult.sleepData.isNotEmpty()) {
                    val monthlySleepByDate = monthlyResult.sleepData.groupBy {
                        Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                    }
                    val totalMonthlySleepMillis =
                        monthlyResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }

                    val avgDuration =
                        Duration.ofMillis(totalMonthlySleepMillis / daysInCurrentMonth)
                    String.format(
                        Locale.KOREA,
                        "%dh %02dm",
                        avgDuration.toHours(),
                        avgDuration.toMinutes() % 60
                    )
                } else "0h 00m"

                val highLowSleepCompare =
                    calculateHighLowCaffeineSleepCompare(
                        caffeineRecords = monthlyResult.caffeineRecords,
                        sleepData = monthlyResult.sleepData,
                        zoneId = zoneId
                    )

                //전체 기간 통합 평균 및 BEST / WORST 월 분석
                val totalAvgSleepStr = if (trendResult.sleepData.isNotEmpty()) {
                    val totalSleepByDate = trendResult.sleepData.groupBy {
                        Instant.ofEpochMilli(it.sleepEnd).atZone(zoneId).toLocalDate()
                    }
                    val totalSleepMillis =
                        trendResult.sleepData.sumOf { it.sleepEnd - it.sleepStart }

                    val avgDuration = Duration.ofMillis(totalSleepMillis / totalSleepByDate.size)
                    String.format(
                        Locale.KOREA,
                        "%dh %02dm",
                        avgDuration.toHours(),
                        avgDuration.toMinutes() % 60
                    )
                } else "0h 00m"

                val monthlySleepTrend =
                    trendResult.sleepData
                        .groupBy { sleep ->
                            Instant.ofEpochMilli(sleep.sleepEnd)
                                .atZone(zoneId)
                                .monthValue
                        }
                        .mapValues { (_, records) ->
                            val totalHours =
                                records.sumOf { sleep ->
                                    (sleep.sleepEnd - sleep.sleepStart) / (1000.0 * 60.0 * 60.0)
                                }

                            totalHours / records.size
                        }
                        .mapKeys { (month, _) ->
                            "${month}월"
                        }
                cachedLateCaffeineCount = lateCaffeineCount
                cachedBedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
                cachedMonthlySleepTrend = monthlySleepTrend
                val monthlyGroups = trendResult.sleepData.groupBy {
                    java.time.YearMonth.from(
                        Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
                    )
                }
                var bestMonth: java.time.YearMonth? = null
                var worstMonth: java.time.YearMonth? = null
                var maxScore = -1
                var minScore = 999

                monthlyGroups.forEach { (yearMonth, records) ->
                    val dailyAvgScores = records.groupBy {
                        Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate()
                    }.map { entry -> entry.value.map { it.deepSleepRatio }.average() }

                    val avgScore = dailyAvgScores.average().toInt()
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

                val periodAvgSleepMillis = periodTotalSleepMillis / daysBetween
                val periodAvgSleepStr = String.format(
                    Locale.KOREA, "%dh %02dm",
                    Duration.ofMillis(periodAvgSleepMillis).toHours(),
                    Duration.ofMillis(periodAvgSleepMillis).toMinutes() % 60
                )

                _uiState.update {
                    it.copy(
                        isDbEmpty = false,
                        isLoading = false,
                        totalSleepDaysCount = totalSleepDaysCount,

                        periodTotalCaffeine = periodTotalCaffeine,
                        periodAvgCaffeine = periodAvgCaffeine,
                        periodTotalSleepTime = periodTotalSleepStr,
                        periodAvgSleepTime = periodAvgSleepStr,
                        periodCaffeineRecords = filteredCaffeine,

                        todaySleepTime = todayTimeStr,
                        todaySleepHours = todayHours,
                        todaySleepMinutes = todayMinutes,
                        todaySleepStart = todayStartStr,
                        hasTodayRecord = hasTodayRecord,
                        todayTotalCaffeine = todayTotalCaffeine,
                        todayCaffeineRecords = dailyResult.caffeineRecords,
                        todaySleepRecords = todaySleepRecords,

                        weeklyAvgCaffeine = weeklyAvgCaffeine,
                        weeklyAvgSleepTime = weeklyAvgSleepStr,
                        weeklyCaffeineChartData = weeklyResult.caffeineChartData,
                        weeklySleepChartData = weeklyResult.sleepChartData,
                        weeklyInsight = _uiState.value.weeklyInsight,
                        monthlyInsight = _uiState.value.monthlyInsight,
                        trendInsight = _uiState.value.trendInsight,
                        isWeeklyInsightLoading = !hasLoadedReportInsights,
                        isMonthlyInsightLoading = !hasLoadedReportInsights,
                        isTrendInsightLoading = !hasLoadedReportInsights,
                        isPeriodInsightLoading = !hasLoadedReportInsights,
                        monthlyCaffeineTrend = trendResult.monthlyCaffeineChartData,
                        monthlyAvgCaffeine = monthlyAvgCaffeine,
                        monthlyAvgSleepTime = monthlyAvgSleepStr,
                        totalAvgSleepTime = totalAvgSleepStr,
                        highCaffeineDaySleepTime = highLowSleepCompare.first,
                        lowCaffeineDaySleepTime = highLowSleepCompare.second,
                        bestMonthLabel = bestMonth?.format(
                            DateTimeFormatter.ofPattern(
                                "M월"
                            )
                        ) ?: "데이터 없음",
                        bestMonthScore = if (maxScore != -1) maxScore else 0,
                        worstMonthLabel = worstMonth?.format(
                            DateTimeFormatter.ofPattern(
                                "M월"
                            )
                        ) ?: "데이터 없음",
                        worstMonthScore = if (minScore != 999) minScore else 0
                    )
                }
                if (!hasLoadedReportInsights) {
                    hasLoadedReportInsights = true

                    loadReportInsights(
                        weeklyAvgSleepStr = weeklyAvgSleepStr,
                        lateCaffeineCount = lateCaffeineCount,
                        bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg,
                        monthlyAvgCaffeine = monthlyAvgCaffeine,
                        monthlyAvgSleepStr = monthlyAvgSleepStr,
                        highLowSleepCompare = highLowSleepCompare,
                        monthlyCaffeineTrend = trendResult.monthlyCaffeineChartData,
                        monthlySleepTrend = monthlySleepTrend,
                        totalAvgSleepStr = totalAvgSleepStr
                    )

                    refreshPeriodInsight()
                }
            } catch (e: Exception) {
                val errorMsg = when (e) {
                    is java.lang.IllegalStateException -> "삼성 헬스 연동에 실패했습니다. 권한 설정을 확인해 주세요."
                    is android.database.sqlite.SQLiteException -> "데이터베이스 읽기 오류가 발생했습니다."
                    else -> "리포트 데이터를 불러오는 중 알 수 없는 오류가 발생했습니다."
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isWeeklyInsightLoading = false,
                        isMonthlyInsightLoading = false,
                        isTrendInsightLoading = false,
                        isPeriodInsightLoading = false,
                        isError = true,
                        errorMessage = errorMsg
                    )
                }
            }
        }
    }

    private fun calculateHighLowCaffeineSleepCompare(
        caffeineRecords: List<com.snoffee.app.domain.model.CaffeineRecord>,
        sleepData: List<SleepData>,
        zoneId: ZoneId
    ): Pair<String, String> {
        if (caffeineRecords.isEmpty() || sleepData.isEmpty()) {
            return "0h 00m" to "0h 00m"
        }

        val caffeineByDate =
            caffeineRecords.groupBy { record ->
                Instant.ofEpochMilli(record.consumedAt)
                    .atZone(zoneId)
                    .toLocalDate()
            }.mapValues { entry ->
                entry.value.sumOf { record ->
                    record.intakeCaffeine
                }
            }

        val sleepByDate =
            sleepData.groupBy { sleep ->
                Instant.ofEpochMilli(sleep.sleepEnd)
                    .atZone(zoneId)
                    .toLocalDate()
            }.mapValues { entry ->
                entry.value.sumOf { sleep ->
                    sleep.sleepEnd - sleep.sleepStart
                }
            }

        val matchedDates =
            caffeineByDate.keys.intersect(sleepByDate.keys)

        if (matchedDates.isEmpty()) {
            return "0h 00m" to "0h 00m"
        }

        val highCaffeineDate =
            matchedDates.maxByOrNull { date ->
                caffeineByDate[date] ?: 0.0
            }

        val lowCaffeineDate =
            matchedDates.minByOrNull { date ->
                caffeineByDate[date] ?: 0.0
            }

        val highSleepMillis =
            highCaffeineDate?.let { sleepByDate[it] } ?: 0L

        val lowSleepMillis =
            lowCaffeineDate?.let { sleepByDate[it] } ?: 0L

        return formatSleepMillis(highSleepMillis) to formatSleepMillis(lowSleepMillis)
    }

    private fun formatSleepMillis(
        millis: Long
    ): String {
        val duration = Duration.ofMillis(millis)

        return String.format(
            Locale.KOREA,
            "%dh %02dm",
            duration.toHours(),
            duration.toMinutes() % 60
        )
    }

    private fun calculatePeriodData(start: LocalDate, end: LocalDate) {
        _uiState.update {
            it.copy(
                isPeriodInsightLoading = true
            )
        }
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

            val avgSleepMillis = totalSleepMillis / daysBetween
            val avgSleepStr = String.format(
                Locale.KOREA,
                "%dh %02dm",
                Duration.ofMillis(avgSleepMillis).toHours(),
                Duration.ofMillis(avgSleepMillis).toMinutes() % 60
            )
            val periodInsight =
                getPeriodInsightUseCase(
                    periodTotalCaffeine = totalCaffeine,
                    periodAvgCaffeine = avgCaffeine,
                    periodTotalSleepTime = totalSleepStr,
                    periodAvgSleepTime = avgSleepStr
                )

            _uiState.update {
                it.copy(
                    periodTotalCaffeine = totalCaffeine,
                    periodAvgCaffeine = avgCaffeine,
                    periodTotalSleepTime = totalSleepStr,
                    periodAvgSleepTime = avgSleepStr,
                    periodInsight = periodInsight,
                    periodCaffeineRecords = filteredCaffeine,
                    isPeriodInsightLoading = false
                )
            }
        }
    }

    private fun loadReportInsights(
        weeklyAvgSleepStr: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int,
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepStr: String,
        highLowSleepCompare: Pair<String, String>,
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepStr: String
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isWeeklyInsightLoading = true,
                    isMonthlyInsightLoading = true,
                    isTrendInsightLoading = true
                )
            }

            val weeklyDeferred = async {
                getWeeklyInsightUseCase(
                    averageSleepTime = weeklyAvgSleepStr,
                    lateCaffeineCount = lateCaffeineCount,
                    bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
                )
            }

            val monthlyDeferred = async {
                getMonthlyInsightUseCase(
                    monthlyAvgCaffeine = monthlyAvgCaffeine,
                    monthlyAvgSleepTime = monthlyAvgSleepStr,
                    highCaffeineDaySleepTime = highLowSleepCompare.first,
                    lowCaffeineDaySleepTime = highLowSleepCompare.second
                )
            }

            val trendDeferred = async {
                getTrendInsightUseCase(
                    monthlyCaffeineTrend = monthlyCaffeineTrend,
                    monthlySleepTrend = monthlySleepTrend,
                    totalAvgSleepTime = totalAvgSleepStr
                )
            }

            _uiState.update {
                it.copy(
                    weeklyInsight = weeklyDeferred.await(),
                    monthlyInsight = monthlyDeferred.await(),
                    trendInsight = trendDeferred.await(),
                    isWeeklyInsightLoading = false,
                    isMonthlyInsightLoading = false,
                    isTrendInsightLoading = false
                )
            }
        }
    }

    private suspend fun calculateBedtimeResidualCaffeineMg(
        caffeineRecords: List<CaffeineRecord>,
        sleepData: List<SleepData>
    ): Int {
        if (caffeineRecords.isEmpty() || sleepData.isEmpty()) {
            return 0
        }

        val halfLifeHours =
            userProfileRepository
                .getUserProfile()
                ?.sensitivity
                ?.halfLifeHours
                ?: CaffeineSensitivity.NORMAL.halfLifeHours

        var totalResidualMg = 0.0

        sleepData.forEach { sleep ->

            caffeineRecords
                .filter { it.consumedAt <= sleep.sleepStart }
                .forEach { caffeine ->

                    totalResidualMg +=
                        caffeineCalculator.calculateResidualCaffeine(
                            intakeCaffeine = caffeine.intakeCaffeine,
                            consumedAt = caffeine.consumedAt,
                            currentTimeMillis = sleep.sleepStart,
                            halfLifeHours = halfLifeHours
                        )
                }
        }

        return totalResidualMg.toInt()
    }

    fun refreshWeeklyInsight() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isWeeklyInsightLoading = true)
            }

            val state = _uiState.value

            val insight = getWeeklyInsightUseCase(
                averageSleepTime = state.weeklyAvgSleepTime,
                lateCaffeineCount = cachedLateCaffeineCount,
                bedtimeResidualCaffeineMg = cachedBedtimeResidualCaffeineMg
            )

            _uiState.update {
                it.copy(
                    weeklyInsight = insight,
                    isWeeklyInsightLoading = false
                )
            }
        }
    }

    fun refreshMonthlyInsight() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isMonthlyInsightLoading = true)
            }

            val state = _uiState.value

            val insight = getMonthlyInsightUseCase(
                monthlyAvgCaffeine = state.monthlyAvgCaffeine,
                monthlyAvgSleepTime = state.monthlyAvgSleepTime,
                highCaffeineDaySleepTime = state.highCaffeineDaySleepTime,
                lowCaffeineDaySleepTime = state.lowCaffeineDaySleepTime
            )

            _uiState.update {
                it.copy(
                    monthlyInsight = insight,
                    isMonthlyInsightLoading = false
                )
            }
        }
    }

    fun refreshTrendInsight() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isTrendInsightLoading = true)
            }

            val state = _uiState.value

            val insight = getTrendInsightUseCase(
                monthlyCaffeineTrend = state.monthlyCaffeineTrend,
                monthlySleepTrend = cachedMonthlySleepTrend,
                totalAvgSleepTime = state.totalAvgSleepTime
            )

            _uiState.update {
                it.copy(
                    trendInsight = insight,
                    isTrendInsightLoading = false
                )
            }
        }
    }

    fun refreshPeriodInsight() {
        calculatePeriodData(
            start = _uiState.value.startDate,
            end = _uiState.value.endDate
        )
    }
}