package com.snoffee.app.presentation.sleep

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class SleepUiState(
    //실제 DB에서 가져온 수면 기록 목록 추가하기
    val currentYearMonth: YearMonth = YearMonth.now(),
    val averageSleepTime: String = "7h 15m",
    val averageScore: Int = 85,
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyScores: Map<LocalDate, Int> = emptyMap(),
    val dailySleepTimes: Map<LocalDate, String> = emptyMap()
)

@HiltViewModel
class SleepViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState = _uiState.asStateFlow()

    // 초기 가상 데이터 (테스트용 - 나중에 DB 연동 시 이 부분을 업데이트)
    init {
        _uiState.value = _uiState.value.copy(
            dailyScores = mapOf(
                LocalDate.now() to 90,           // 오늘: 90점 (좋음)
                LocalDate.now().minusDays(1) to 45  // 어제: 45점 (부족)
            )
        )
    }
    fun onPrevMonth() {
        _uiState.value = _uiState.value.copy(
            currentYearMonth = _uiState.value.currentYearMonth.minusMonths(1)
        )
    }

    fun onNextMonth() {
        _uiState.value = _uiState.value.copy(
            currentYearMonth = _uiState.value.currentYearMonth.plusMonths(1)
        )
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
}