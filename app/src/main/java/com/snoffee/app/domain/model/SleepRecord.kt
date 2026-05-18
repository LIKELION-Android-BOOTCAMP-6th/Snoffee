package com.snoffee.app.domain.model

data class SleepRecord(
    val date: Long,           // 기준 날짜 (Timestamp)
    val startTime: Long,      // 취침 시각 (Timestamp)
    val endTime: Long,        // 기상 시각 (Timestamp)
    val satisfaction: Int  // 사용자가 선택한 만족도 (1~5)
)
