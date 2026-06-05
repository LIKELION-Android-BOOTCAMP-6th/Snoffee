package com.snoffee.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.data.wear.PhoneDataClient
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateResidualUseCase: CalculateResidualUseCase,
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
    private val phoneDataClient: PhoneDataClient
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(HomeUiState(isLoading = true))
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

                //워치
                val currentRiskLevel = getRiskLevel(residualDouble)
                val currentConcentrationLevel = when {
                    residualDouble >= 150.0 -> "높음"
                    residualDouble >= 50.0 -> "보통"
                    residualDouble > targetMinCaffeine -> "낮음"
                    else -> "-"
                }
                phoneDataClient.sendCaffeineStateToWatch(
                    residualMg = residualDouble,
                    riskLevel = currentRiskLevel.name, // "SAFE", "CAUTION", "DANGER"
                    metabolismTime = formattedTime,
                    concentrationLevel = currentConcentrationLevel
                )


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