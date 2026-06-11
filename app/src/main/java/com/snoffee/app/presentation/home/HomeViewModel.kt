package com.snoffee.app.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.wear.PhoneDataClient
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.SleepRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import com.snoffee.app.service.PhoneNotificationHelper
import com.snoffee.app.domain.usecase.gemini.GetHomeInsightUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    @param:ApplicationContext private val context: Context,
    private val calculateResidualUseCase: CalculateResidualUseCase,
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
    private val getHomeInsightUseCase: GetHomeInsightUseCase,
    private val sleepRepository: SleepRepository,
    private val caffeineRepository: CaffeineRepository,
    private val phoneDataClient: PhoneDataClient,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()
    private var refreshJob: Job? = null
    private var observeJob: Job? = null

    init {
        observeCaffeineDatabase()
        startResidualRefresh()
        loadHomeInsight()
    }

    private fun observeCaffeineDatabase() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            getTodayCaffeineUseCase().collect { todayRecords ->
                calculateAndEmitCaffeineState(todayRecords)
            }
        }
    }
    private fun startResidualRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (true) {
                delay(1 * 60 * 1000L)
                loadResidualCaffeine()
            }
        }
    }
    fun loadResidualCaffeine() {
        viewModelScope.launch {
            runCatching {
                val todayRecords = getTodayCaffeineUseCase().first()
                calculateAndEmitCaffeineState(todayRecords)
            }
        }
    }

    private suspend fun calculateAndEmitCaffeineState(todayRecords: List<com.snoffee.app.domain.model.CaffeineRecord>) {
        _uiState.update { state ->
            state.copy(
                isLoading = state.recentLogs.isEmpty() && state.isLoading,
                errorMessage = null
            )
        }

        runCatching {
            val residualAnalysis = calculateResidualUseCase()
            val recentFiveLogs = todayRecords
                .sortedByDescending { it.consumedAt }
                .take(5)

            val profile = userProfileRepository.getUserProfile()
                ?: UserProfile(1, 70.0, 175.0, 400.0, false, 0L, 0L, CaffeineSensitivity.NORMAL, 0L)

            Triple(residualAnalysis, recentFiveLogs, profile)
        }.onSuccess { (analysis, recentLogs, profile) ->
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

            // 워치 연동 패킷 방출
            val currentRiskLevel = getRiskLevel(residualDouble)
            val currentConcentrationLevel = when {
                residualDouble >= 150.0 -> "높음"
                residualDouble >= 50.0 -> "보통"
                residualDouble > targetMinCaffeine -> "낮음"
                else -> "-"
            }
            phoneDataClient.sendCaffeineStateToWatch(
                residualMg = residualDouble,
                riskLevel = currentRiskLevel.name,
                metabolismTime = formattedTime,
                concentrationLevel = currentConcentrationLevel,
                sensitivity = profile.sensitivity.name,
                targetSleepTime = profile.userSleepTime
            )

            // 폰 실시간 UI 업데이트 트리거 가동
            _uiState.update { state ->
                state.copy(
                    residualCaffeineMg = residualDouble,
                    riskLevel = currentRiskLevel,
                    isLoading = false,
                    isEmpty = residualDouble <= targetMinCaffeine && recentLogs.isEmpty(),
                    metabolismTime = formattedTime,
                    concentrationLevel = currentConcentrationLevel,
                    recentLogs = recentLogs,
                    lastUpdated = System.currentTimeMillis()
                )
            }
            val halfLife = when (profile.sensitivity) {
                CaffeineSensitivity.SENSITIVE -> 6.0
                CaffeineSensitivity.NORMAL -> 5.0
                CaffeineSensitivity.LOW -> 4.0
                else -> 5.0
            }

            PhoneNotificationHelper.schedulePhoneAlarms(
                context = context,
                currentResidual = residualDouble,
                halfLifeHours = halfLife,
                targetBedTimeMillis = profile.userSleepTime,
                isNotificationEnabled = true
            )
        }.onFailure { throwable ->
            _uiState.update { state ->
                state.copy(
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
                    caffeineRepository.getCaffeineRecordsSince(sevenDaysAgo).first()

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
        observeJob?.cancel()
        super.onCleared()
    }
}