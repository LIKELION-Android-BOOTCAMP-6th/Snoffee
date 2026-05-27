package com.snoffee.app.presentation.caffeine.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.usecase.caffeine.DeleteCaffeineUseCase
import com.snoffee.app.domain.usecase.caffeine.EditCaffeineUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CaffeineMainViewModel @Inject constructor(
    private val getTodayCaffeineUseCase: GetTodayCaffeineUseCase,
    private val deleteCaffeineUseCase: DeleteCaffeineUseCase,
    private val editCaffeineUseCase: EditCaffeineUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CaffeineMainUiState())
    val uiState: StateFlow<CaffeineMainUiState> = _uiState.asStateFlow()
    private var recordsJob: Job? = null

    init {
        val today = LocalDate.now()
        observeMonthRecords(YearMonth.from(today)) // 이번 달 dot 초기화
        observeRecordsByDate(today)                // 오늘 기록 초기화
    }

    // 이번 달 전체 dot 표시용
    private fun observeMonthRecords(yearMonth: YearMonth) {
        getTodayCaffeineUseCase(yearMonth) // repository 대신 UseCase 호출
            .onEach { records ->
                _uiState.update { state ->
                    state.copy(
                        recordedDates = records
                            .map { LocalDate.ofEpochDay(it.consumedAt / 86400000) }
                            .toSet()
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    // 선택한 날짜 기록용
    private fun observeRecordsByDate(date: LocalDate) {
        recordsJob?.cancel()
        recordsJob = getTodayCaffeineUseCase(date)
            .onEach { records ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        todayRecords = records
                    )
                }
            }
            .catch { throwable ->
                _uiState.update {
                    it.copy(error = throwable.message ?: "알 수 없는 오류가 발생했어요")
                }
            }
            .launchIn(viewModelScope)
    }

    // 캘린더 날짜 선택
    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                currentYearMonth = YearMonth.from(date),
            )
        }
        observeRecordsByDate(date)     // 선택 날짜 기준 조회로 교체
    }

    // 이전 달 이동
    fun onPrevMonth() {
        val newMonth = _uiState.value.currentYearMonth.minusMonths(1)
        _uiState.update { it.copy(currentYearMonth = newMonth) }
        observeMonthRecords(newMonth) // 월 변경 시 dot 갱신
    }

    //  다음 달 이동
    fun onNextMonth() {
        val newMonth = _uiState.value.currentYearMonth.plusMonths(1)
        _uiState.update { it.copy(currentYearMonth = newMonth) }
        observeMonthRecords(newMonth) // 월 변경 시 dot 갱신
    }

    // 에러 초기화
    fun onErrorDismiss() {
        _uiState.update { it.copy(error = null) }
    }

    // 카페인 섭취 기록 삭제
    fun deleteRecord(id: Long) {
        viewModelScope.launch {
            runCatching {
                deleteCaffeineUseCase(id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(error = throwable.message ?: "삭제 중 오류가 발생했습니다.")
                }
            }
        }
    }

    // 기록된 음료 수정
    fun editCaffeineRecord(record: CaffeineRecord) {
        viewModelScope.launch {
            runCatching {
                editCaffeineUseCase(record)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(error = throwable.message ?: "수정 중 오류가 발생했습니다.")
                }
            }
        }
    }
}
