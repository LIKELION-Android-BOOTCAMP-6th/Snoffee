package com.snoffee.app.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.wear.PhoneDataClient
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import com.snoffee.app.service.PhoneNotificationHelper
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val calculateResidualUseCase: CalculateResidualUseCase,
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
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