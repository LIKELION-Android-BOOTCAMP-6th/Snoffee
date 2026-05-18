package com.snoffee.app.domain.model

// 카페인 분석 결과
data class CaffeineAnalysis(
    val residualAmount: Double,             // 현재 체내 카페인 잔류량 (mg)
    val cutoffTime: Long,                   // 개인화 카페인 컷오프 시각
    val sleepImpactScore: Double            // 수면 영향 점수 (0.0 ~ 1.0)
)