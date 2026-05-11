package com.snoffee.app.domain.model

import java.time.LocalDateTime

// 카페인 섭취 기록
data class CaffeineRecord(
    val id: Long = 0,                       // PK, 자동생성
    val drinkId: String,                    // 음료 ID
    val drinkName: String,                  // 음료명
    val brandName: String,                  // 브랜드명
    val intakeSize: Double,                 // 섭취 용량
    val intakeCaffeine: Double,             // 섭취 카페인 (mg)
    val consumedAt: Long                    // 섭취 시각
)