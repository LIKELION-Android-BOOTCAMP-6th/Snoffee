package com.snoffee.app.data.model

// 카페인 섭취 기록 DTO
// Room DB에서 가져온 데이터 형태. CaffeineMapper를 통해 CaffeineRecord로 변환
data class CaffeineRecordDto(
    val id: Long = 0,                       // PK, 자동생성
    val drinkId: String,                    // 음료 ID
    val drinkName: String,                  // 음료명
    val brandName: String,                  // 브랜드명
    val intakeSize: Double,                 // 섭취 용량
    val intakeCaffeine: Double,             // 섭취 카페인 (mg)
    val consumedAt: Long           // 섭취 시각
)