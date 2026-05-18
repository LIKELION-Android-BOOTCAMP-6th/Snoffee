package com.snoffee.app.domain.model

// 수면 데이터
// 삼성헬스 SDK에서 가져온 수면 데이터를 도메인에서 사용하는 형태
data class SleepData(
    val id: Long = 0,
    val date: Long,                         // 수면 날짜 (PK)
    val sleepStart: Long,                   // 수면 시작 시각
    val sleepEnd: Long,                     // 수면 종료 시각
    val deepSleepRatio: Double,              // 깊은 수면 비율 (0.0 ~ 1.0)
    val source: String = "manual"
)