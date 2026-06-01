package com.snoffee.app.domain.model


// 카페인 수면 상관관계 변수
data class CaffeineSleepCorrelation(
    val lateNightCaffeineCount: Int,      // 19:00 이후 섭취 횟수
    val beforeSleepCaffeineCount: Int,    // 취침 3시간 이내 섭취 횟수
    val residualCaffeineAtSleep: Double   // 취침 시점 체내 잔류량 (mg)
)