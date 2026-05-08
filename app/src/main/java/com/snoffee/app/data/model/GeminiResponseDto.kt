package com.snoffee.app.data.model

// Gemini API 응답 DTO
// Gemini API에서 오는 응답 데이터 형태. GeminiMapper를 통해 CaffeineAnalysis로 변환
data class GeminiResponseDto(
    val residualAmount: Double,             // 현재 체내 카페인 잔류량 (mg)
    val cutoffTime: String,                 // 개인화 카페인 컷오프 시각 (ISO 8601 형식)
    val sleepImpactScore: Double,           // 수면 영향 점수 (0.0 ~ 1.0)
    val insight: String?                    // 주간 자연어 인사이트 (없을 수 있음)
)