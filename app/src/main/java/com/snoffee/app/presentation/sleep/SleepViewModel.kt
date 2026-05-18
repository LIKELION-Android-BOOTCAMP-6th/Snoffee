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
    val dailySleepTimes: Map<LocalDate, String> = emptyMap(),

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
    private var currentMonthRawData = mapOf<LocalDate, SleepData>()

    init {
        //데이터 로드
        refreshSleepData()
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

            // 캘린더 UI에 맞게 Map 데이터 형태로 가공하기
            val scoresMap = mutableMapOf<LocalDate, Int>()
            val timesMap = mutableMapOf<LocalDate, String>()
            val rawDataMap = mutableMapOf<LocalDate, SleepData>()

            var totalScore = 0
            var totalSleepMillis = 0L
            var recordCount = 0

            if (sleepList.isNotEmpty()) {
                sleepList.forEach { sleepData ->
                    if (sleepData.deepSleepRatio > 0) {
                        // 밀리초 타임스탬프를 LocalDate로 역변환
                        val localDate =
                            Instant.ofEpochMilli(sleepData.date).atZone(zoneId).toLocalDate()

                        rawDataMap[localDate] = sleepData
                        val finalScore = sleepData.deepSleepRatio
                        scoresMap[localDate] = finalScore

                        //날짜별 총 수면 시간 계산 (종료 시간 - 시작 시간)
                        val durationMillis = sleepData.sleepEnd - sleepData.sleepStart
                        val duration = Duration.ofMillis(durationMillis)
                        val hours = duration.toHours()
                        val minutes = duration.toMinutes() % 60
                        timesMap[localDate] = "${hours}h ${minutes}m"

                        totalScore += finalScore
                        totalSleepMillis += durationMillis
                        recordCount++
                    }
                }
            }
            currentMonthRawData = rawDataMap

            //월 평균 계산
            val avgScore = if (recordCount > 0) totalScore / recordCount else 0
            val avgTimeLabel = if (recordCount > 0) {
                val averageSleepMillis = totalSleepMillis / recordCount
                val avgDuration = Duration.ofMillis(averageSleepMillis)

                val hours = avgDuration.toHours()
                val minutes = avgDuration.toMinutes() % 60

                String.format(Locale.KOREA, "%dh %02dm", hours, minutes)

            } else {
                "0h 00m"
            }

            // 가공 완료된 실시간 데이터들로 UI State를 최종 업데이트
            _uiState.update {
                it.copy(
                    dailyScores = scoresMap,
                    dailySleepTimes = timesMap,
                    averageScore = avgScore,
                    averageSleepTime = avgTimeLabel
                )
            }
        }
    }

    fun getOriginalSleepData(date: LocalDate): SleepData? {
        return currentMonthRawData[date]
    }
    fun onPrevMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.minusMonths(1))
        }
        refreshSleepData()
    }

    fun onNextMonth() {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.plusMonths(1))
        }
        refreshSleepData()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(selectedDate = date)
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

    fun deleteSleepRecord(data: LocalDate) {
        viewModelScope.launch {
            val zoneId = ZoneId.systemDefault()
            val startOfDayMillis = data.atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endOfDayMillis = data.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

            val dayRecords =
                sleepRepository.getSleepDataByDateRange(startOfDayMillis, endOfDayMillis)

            if (dayRecords.isNotEmpty()) {
                val result = deleteSleepDataUseCase(dayRecords.first())

                if (result.isSuccess) {
                    refreshSleepData()
                } else {
                    _uiState.update { it.copy(isSavingError = true) }
                }
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