package com.snoffee.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateResidualUseCase: CalculateResidualUseCase,
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            HomeUiState(
                residualCaffeineMg = 0.0,
                isEmpty = true,
                isLoading = false
            )
        )
    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()
    private var refreshJob: Job? = null
    init {
        startResidualRefresh()
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
            _uiState.value = _uiState.value.copy(
                isLoading = _uiState.value.recentLogs.isEmpty(),
                errorMessage = null
            )
            runCatching {
                val residualAnalysis = calculateResidualUseCase()
                val todayRecords = getTodayCaffeineUseCase()

                val recentFiveLogs = todayRecords
                    .sortedByDescending { it.consumedAt }
                    .take(5)

                Pair(residualAnalysis, recentFiveLogs)
            }.onSuccess { (analysis, recentLogs) ->
                val formattedTime = if (recentLogs.isEmpty()) {
                    "--:--"
                } else {
                    SimpleDateFormat("a hh:mm", Locale.KOREAN).format(Date(analysis.cutoffTime))
                }

                val residualDouble = analysis.residualAmount

                _uiState.value = _uiState.value.copy(
                    residualCaffeineMg = residualDouble,
                    riskLevel = getRiskLevel(residualDouble),
                    isLoading = false,
                    isEmpty = residualDouble <= 0 && recentLogs.isEmpty(),
                    metabolismTime = formattedTime,
                    concentrationLevel = if (residualDouble >= 150) "높음" else if (residualDouble >= 50) "보통" else "낮음",
                    recentLogs = recentLogs
                )
            }.onFailure { throwable ->
                _uiState.value = _uiState.value.copy(
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
        super.onCleared()
    }
}