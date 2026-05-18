package com.snoffee.app.presentation.report

import java.time.LocalDate

data class ReportUiState(
    //일간 데이터
    val todaySleepTime: String = "0h 00m",
    val todaySleepHours: Int = 0,
    val todaySleepMinutes: Int = 0,
    val todaySleepStart: String = "--:--",
    val hasTodayRecord: Boolean = false,

    //주간 데이터 영역
    val weeklyAvgSleepTime: String = "0h 00m",
    val weeklyScores: Map<LocalDate, Int> = emptyMap(), // 요일별 바 차트 바인딩용

    //월간 데이터 영역
    val monthlyAvgSleepTime: String = "0h 00m",
    val highCaffeineDaySleepTime: String = "0h 00m", // 대조군: 카페인 과다일 수면 평균
    val lowCaffeineDaySleepTime: String = "0h 00m",  // 대조군: 카페인 최소일 수면 평균

    //전체 추이 영역
    val totalAvgSleepTime: String = "0h 00m",
    val bestMonthLabel: String = "데이터 없음",
    val bestMonthScore: Int = 0,
    val worstMonthLabel: String = "데이터 없음",
    val worstMonthScore: Int = 0,

    // 공통 비어있음 분기점 검증용
    val isDbEmpty: Boolean = true
)