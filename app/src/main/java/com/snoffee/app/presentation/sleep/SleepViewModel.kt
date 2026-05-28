package com.snoffee.app.presentation.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.usecase.sleep.DeleteSleepDataUseCase
import com.snoffee.app.domain.usecase.sleep.SaveSleepDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

data class SleepUiState(
    //실제 DB에서 가져온 수면 기록 목록 추가하기
    val currentYearMonth: YearMonth = YearMonth.now(),
    val averageSleepTime: String = "0h 00m",
    val averageScore: Int = 0,
    val selectedDate: LocalDate = LocalDate.now(),
    val dailyScores: Map<LocalDate, Int> = emptyMap(),
    val selectedDateRecords: List<SleepData> = emptyList(),
    val hasHealthPermission: Boolean = false,

    //에러 or 성공 상태
    val isSavingError: Boolean = false,
    val isSaveSuccess: Boolean = false
)

@HiltViewModel
class SleepViewModel @Inject constructor(
    private val saveSleepRecordUseCase: SaveSleepDataUseCase,
    private val deleteSleepDataUseCase: DeleteSleepDataUseCase,
    private val sleepRepository: com.snoffee.app.domain.repository.SleepRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState())
    val uiState = _uiState.asStateFlow()

    //에러 시 재시도 위한 데이터 보관
    private var pendingRecord: SleepData? = null

    //이번 달 데이터 원본 객체 저장 보관함
    private var currentMonthRawData = mapOf<LocalDate, List<SleepData>>()

    init {
        //데이터 로드
        refreshSleepData()
        checkHealthPermission()
    }

    private fun refreshSleepData() {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()

            // 현재 선택된 '월'의 시작 시각과 끝 시각을 밀리초로 계산해서 해당 월의 데이터 로드
            val currentMonth = _uiState.value.currentYearMonth
            val startMillis = currentMonth.atDay(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis =
                currentMonth.atEndOfMonth().atTime(23, 59, 59).atZone(zoneId).toInstant()
                    .toEpochMilli()

            // DB(Room + 삼성헬스 통합)에서 이번 달 데이터 리스트 가져오기
            val sleepList = sleepRepository.getSleepDataByDateRange(startMillis, endMillis)

            // 날짜별 리스트 그룹화
            val groupedData = sleepList
                .filter { it.deepSleepRatio > 0 }
                .groupBy { Instant.ofEpochMilli(it.date).atZone(zoneId).toLocalDate() }

            currentMonthRawData = groupedData

            // 캘린더 UI에 맞게 Map 데이터 형태로 가공하기
            val scoresMap = mutableMapOf<LocalDate, Int>()
            var totalMonthlyScore = 0
            var totalMonthlySleepMillis = 0L
            val activeDaysCount = groupedData.size

            groupedData.forEach { (localDate, records) ->
                // 하루에 기록이 여러 개일 경우, 캘린더 셀에 평균 점수
                val dayAvgScore = records.map { it.deepSleepRatio }.average().toInt()
                scoresMap[localDate] = dayAvgScore

                val dayTotalSleepMillis = records.sumOf { it.sleepEnd - it.sleepStart }

                totalMonthlyScore += dayAvgScore
                totalMonthlySleepMillis += dayTotalSleepMillis
            }
            val avgScore = if (activeDaysCount > 0) totalMonthlyScore / activeDaysCount else 0
            val avgTimeLabel = if (activeDaysCount > 0) {
                // 하루 평균 수면 밀리초 = 당월 총 수면 시간 / 기록이 있는 일수
                val averageSleepMillisPerDay = totalMonthlySleepMillis / activeDaysCount
                val avgDuration = Duration.ofMillis(averageSleepMillisPerDay)

                val hours = avgDuration.toHours()
                val minutes = avgDuration.toMinutes() % 60
                String.format(Locale.KOREA, "%dh %02dm", hours, minutes)
            } else {
                "0h 00m"
            }
            _uiState.update {
                it.copy(
                    dailyScores = scoresMap,
                    selectedDateRecords = groupedData[it.selectedDate] ?: emptyList(),
                    averageScore = avgScore,
                    averageSleepTime = avgTimeLabel
                )
            }
        }
    }

    fun checkHealthPermission() {
        viewModelScope.launch {
            val hasPermission = sleepRepository.hasHealthPermission()

            _uiState.update {
                it.copy(
                    hasHealthPermission = hasPermission
                )
            }
        }
    }

    fun onPrevMonth() {
        _uiState.update {
            val nextMonth = it.currentYearMonth.minusMonths(1)
            it.copy(
                currentYearMonth = nextMonth,
                selectedDate = nextMonth.atDay(1)
            )
        }
        refreshSleepData()
    }

    fun onNextMonth() {
        _uiState.update {
            val nextMonth = it.currentYearMonth.plusMonths(1)
            it.copy(
                currentYearMonth = nextMonth,
                selectedDate = nextMonth.atDay(1)
            )
        }
        refreshSleepData()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                selectedDateRecords = currentMonthRawData[date] ?: emptyList()
            )
        }
    }

    fun saveSleepRecord(record: SleepData) {
        pendingRecord = record
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingError = false, isSaveSuccess = false) }

            val result = saveSleepRecordUseCase(record)

            if (result.isSuccess) {
                pendingRecord = null
                _uiState.update { it.copy(isSaveSuccess = true) }
                refreshSleepData()
            } else {
                _uiState.update { it.copy(isSavingError = true) }
            }
        }
    }

    fun deleteSleepRecord(record: SleepData) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingError = false) }
            val result = deleteSleepDataUseCase(record)

            if (result.isSuccess) {
                refreshSleepData()
            } else {
                _uiState.update { it.copy(isSavingError = true) }
            }
        }
    }

    fun resetErrorState() {
        _uiState.update { it.copy(isSavingError = false) }
    }

    fun retrySave() {
        pendingRecord?.let { saveSleepRecord(it) }
    }

    fun resetState() {
        _uiState.update { it.copy(isSavingError = false, isSaveSuccess = false) }
        pendingRecord = null
    }
}