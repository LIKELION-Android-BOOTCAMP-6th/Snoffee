package com.snoffee.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.SleepRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import com.snoffee.app.domain.usecase.gemini.GetHomeInsightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateResidualUseCase: CalculateResidualUseCase,
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
    private val getHomeInsightUseCase: GetHomeInsightUseCase,
    private val sleepRepository: SleepRepository,
    private val caffeineRepository: CaffeineRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()
    private var refreshJob: Job? = null
    init {
        startResidualRefresh()
        loadHomeInsight()
    }
    private fun startResidualRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                loadResidualCaffeine()
                delay(5 * 60 * 1000L)
            }
        }
    }
    fun loadResidualCaffeine() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = it.recentLogs.isEmpty(),
                    errorMessage = null
                )
            }
            runCatching {
                val residualAnalysis = calculateResidualUseCase()
                val todayRecords = getTodayCaffeineUseCase().first()

                val recentFiveLogs = todayRecords
                    .sortedByDescending { it.consumedAt }
                    .take(5)

                Pair(residualAnalysis, recentFiveLogs)
            }.onSuccess { (analysis, recentLogs) ->
                val residualDouble = analysis.residualAmount
                val targetMinCaffeine = 10.0

                val formattedTime = if (residualDouble <= targetMinCaffeine) {
                    "--:--"
                } else {
                    SimpleDateFormat(
                        "M월 d일 (E) a h시 m분",
                        Locale.KOREAN
                    ).format(Date(analysis.cutoffTime))
                }

                _uiState.update { state ->
                    state.copy(
                        residualCaffeineMg = residualDouble,
                        riskLevel = getRiskLevel(residualDouble),
                        isLoading = false,
                        isEmpty = residualDouble <= targetMinCaffeine && recentLogs.isEmpty(),
                        metabolismTime = formattedTime,
                        concentrationLevel = when {
                            residualDouble >= 150.0 -> "높음"
                            residualDouble >= 50.0 -> "보통"
                            residualDouble > targetMinCaffeine -> "낮음"
                            else -> "-"
                        },
                        recentLogs = recentLogs,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = throwable.message ?: "잔류량 계산 실패"
                )
            }
        }
    }
    fun loadHomeInsight(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentState = _uiState.value

            if (!forceRefresh && currentState.hasLoadedInsight) {
                return@launch
            }

            _uiState.update {
                it.copy(
                    isInsightLoading = true,
                    insightErrorMessage = null
                )
            }

            runCatching {
                val now = System.currentTimeMillis()
                val sevenDaysAgo = now - (7L * 24L * 60L * 60L * 1000L)
                val beforeSleepHours = 6

                val sleepData =
                    sleepRepository.getSleepDataByDateRange(
                        startTimeMillis = sevenDaysAgo,
                        endTimeMillis = now
                    )

                val weeklyCaffeineRecords =
                    caffeineRepository.getCaffeineRecordsSince(sevenDaysAgo)

                val averageSleepMillis =
                    if (sleepData.isNotEmpty()) {
                        sleepData.map { it.sleepEnd - it.sleepStart }
                            .average()
                            .toLong()
                    } else {
                        0L
                    }

                val averageSleepTime =
                    if (averageSleepMillis > 0) {
                        val hours = averageSleepMillis / (1000 * 60 * 60)
                        val minutes = (averageSleepMillis / (1000 * 60)) % 60
                        "${hours}h ${minutes}m"
                    } else {
                        "0h 00m"
                    }

                val averageSleepScore =
                    if (sleepData.isNotEmpty()) {
                        sleepData.map { it.deepSleepRatio }
                            .average()
                            .toInt()
                    } else {
                        0
                    }

                val lateCaffeineCount =
                    weeklyCaffeineRecords.count { caffeine ->
                        sleepData.any { sleep ->
                            caffeine.consumedAt in
                                    (sleep.sleepStart - beforeSleepHours * 60L * 60L * 1000L)..sleep.sleepStart
                        }
                    }

                val bedtimeResidualCaffeineMg =
                    calculateBedtimeResidualCaffeineMg(
                        caffeineRecords = weeklyCaffeineRecords,
                        sleepData = sleepData
                    )

                getHomeInsightUseCase(
                    recentDays = 7,
                    beforeSleepHours = beforeSleepHours,
                    lateCaffeineCount = lateCaffeineCount,
                    averageSleepTime = averageSleepTime,
                    averageSleepScore = averageSleepScore,
                    bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
                )
            }.onSuccess { insight ->
                _uiState.update {
                    it.copy(
                        homeInsight = insight,
                        isInsightLoading = false,
                        hasLoadedInsight = true
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isInsightLoading = false,
                        insightErrorMessage = throwable.message ?: "인사이트 생성 실패"
                    )
                }
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

        val lookbackMillis = 48 * 60 * 60 * 1000L

        val averageResidual =
            sleepData.map { sleep ->
                caffeineRecords
                    .filter { caffeine ->
                        caffeine.consumedAt <= sleep.sleepStart &&
                                caffeine.consumedAt >= sleep.sleepStart - lookbackMillis
                    }
                    .sumOf { caffeine ->
                        val elapsedHours =
                            (sleep.sleepStart - caffeine.consumedAt) / (1000.0 * 60.0 * 60.0)

                        caffeine.intakeCaffeine *
                                0.5.pow(elapsedHours / halfLifeHours)
                    }
            }.average()

        return averageResidual.toInt()
    }
    private fun getRiskLevel(
        residualMg: Double
    ): CaffeineRiskLevel {
        return when {
            residualMg < 50 ->
                CaffeineRiskLevel.SAFE
            residualMg < 150 ->
                CaffeineRiskLevel.CAUTION
            else ->
                CaffeineRiskLevel.DANGER
        }
    }
    override fun onCleared() {
        refreshJob?.cancel()
        super.onCleared()
    }
}