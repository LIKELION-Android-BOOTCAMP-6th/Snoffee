package com.snoffee.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.usecase.caffeine.CalculateResidualUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val calculateResidualUseCase:
    CalculateResidualUseCase
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            HomeUiState(
                residualCaffeineMg = 0,
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
            _uiState.value =
                _uiState.value.copy(
                    isLoading = true,
                    errorMessage = null
                )
            runCatching {
                calculateResidualUseCase()
            }.onSuccess { residual ->
                _uiState.value =
                    HomeUiState(
                        residualCaffeineMg = residual,
                        riskLevel = getRiskLevel(residual),
                        isLoading = false,
                        isEmpty = residual <= 0,
                        metabolismTime = "02:45 AM", // 추후 DB 값
                        concentrationLevel = "높음",    // 추후 DB 값
                        recentLogs = listOf("더블 에스프레소", "플랫 화이트", "아메리카노") // 추후 DB 값
                    )
            }.onFailure { throwable ->
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage =
                            throwable.message
                                ?: "잔류량 계산 실패"
                    )
            }
        }
    }
    private fun getRiskLevel(
        residualMg: Int
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