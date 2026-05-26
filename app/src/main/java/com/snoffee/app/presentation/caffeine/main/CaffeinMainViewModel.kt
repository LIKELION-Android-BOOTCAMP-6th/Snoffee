package com.snoffee.app.presentation.caffeine.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.usecase.caffeine.DeleteCaffeineUseCase
import com.snoffee.app.domain.usecase.caffeine.EditCaffeineUseCase
import com.snoffee.app.domain.usecase.caffeine.GetTodayCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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

    init {
        observeTodayRecords()
    }

    // Flow 구독. DB 변경 시 자동으로 UI 갱신
    fun observeTodayRecords() {
        getTodayCaffeineUseCase()
            .onEach { records ->
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
            .catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "알 수 없는 오류가 발생했어요"
                    )
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
    }

    // 이전 달 이동
    fun onPrevMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1))
        }
    }

    //  다음 달 이동
    fun onNextMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1))
        }
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
