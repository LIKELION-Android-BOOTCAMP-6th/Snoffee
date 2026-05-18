package com.snoffee.app.data.model

import java.time.LocalDateTime

// 수면 데이터 DTO
// 삼성헬스 SDK에서 가져온 데이터 형태. SleepMapper를 통해 SleepData로 변환
data class SleepDataDto(
    val date: LocalDateTime,                // 수면 날짜 (PK)
    val sleepStart: Long,          // 수면 시작 시각
    val sleepEnd: Long,            // 수면 종료 시각
    val deepSleepRatio: Int             // 깊은 수면 비율 (0.0 ~ 1.0)
)