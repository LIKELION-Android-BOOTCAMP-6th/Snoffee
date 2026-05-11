package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.CaffeineAnalysis

// Gemini API 호출 계약
// 실제 구현은 GeminiRepositoryImpl
interface GeminiRepository {
    suspend fun getCutoffTime(): CaffeineAnalysis           // 개인화 컷오프 시각 산출
    suspend fun getWeeklyInsight(): String                  // 주간 자연어 인사이트 생성
}