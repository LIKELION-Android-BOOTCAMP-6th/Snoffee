package com.snoffee.app.presentation.sleep

import androidx.lifecycle.ViewModel
import com.snoffee.app.presentation.caffeine.CalendarDay
import com.snoffee.app.presentation.caffeine.buildCalendarGrid
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
    val recordedDates: Set<LocalDate> = emptySet()
)

@HiltViewModel
class SleepViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState = _uiState.asStateFlow()

    fun getCalendarGrid(): List<CalendarDay> {
        val state = _uiState.value
        return buildCalendarGrid(
            yearMonth = state.currentYearMonth,
            selectedDate = state.selectedDate,
            recordedDates = state.recordedDates
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