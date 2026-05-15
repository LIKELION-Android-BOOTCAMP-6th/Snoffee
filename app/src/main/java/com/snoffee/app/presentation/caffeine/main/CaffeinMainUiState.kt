package com.snoffee.app.presentation.caffeine.main

import com.snoffee.app.domain.model.CaffeineRecord
import java.time.LocalDate
import java.time.YearMonth

data class CaffeineMainUiState(
    // 로딩 / 에러
    val isLoading: Boolean = false,
    val error: String? = null,

    // 캘린더
    val currentYearMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val recordedDates: Set<LocalDate> = emptySet(),  // 섭취 기록 있는 날짜 (캘린더 점 표시용)

    // 카페인 기록
    val todayRecords: List<CaffeineRecord> = emptyList(),  // 선택된 날짜의 기록

    // 하루 권장 섭취량
    val dailyLimitMg: Double = 400.0,  // 기본값 400mg ( todo :: UserProfile에서 추후 받기 )
) {
    // 자동 계산
    val totalIntakeMg: Double
        get() = todayRecords.sumOf { it.intakeCaffeine }

    val remainMg: Double
        get() = (dailyLimitMg - totalIntakeMg).coerceAtLeast(0.0)

    val progress: Float
        get() = (totalIntakeMg / dailyLimitMg).toFloat().coerceIn(0f, 1f)

    val isEmpty: Boolean
        get() = todayRecords.isEmpty()
}
