package com.snoffee.app.presentation.caffeine.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CaffeineMainViewModel @Inject constructor(
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaffeineMainUiState())
    val uiState: StateFlow<CaffeineMainUiState> = _uiState.asStateFlow()

    init {
        loadTodayRecords()
    }

    // 오늘 기록 로드
    fun loadTodayRecords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getTodayCaffeineUseCase() }
                .onSuccess { records ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            todayRecords = records,
                            // 기록이 있는 날짜 추출 (캘린더 점 표시)
                            recordedDates = state.recordedDates + records
                                .map { record ->
                                    LocalDate.ofEpochDay(record.consumedAt / 86400000)
                                }
                                .toSet(),
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "알 수 없는 오류가 발생했어요",
                        )
                    }
                }
        }
    }

    // 캘린더 날짜 선택
    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentYearMonth = YearMonth.from(date),
            )
        }
        // TODO: 선택한 날짜 기준으로 기록 조회 (날짜별 조회 UseCase 추가 시 연결)
        loadTodayRecords()
    }

    // 이전 달 이동
    fun onPrevMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1))
        }
    }

    //  달 이동
    fun onNextMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1))
        }
    }

    // 에러 초기화
    fun onErrorDismiss() {
        _uiState.update { it.copy(error = null) }
    }
}
