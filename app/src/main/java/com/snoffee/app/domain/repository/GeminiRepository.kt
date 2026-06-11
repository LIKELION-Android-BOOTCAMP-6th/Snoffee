package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.CaffeineAnalysis

// Gemini API 호출 계약
// 실제 구현은 GeminiRepositoryImpl
interface GeminiRepository {
    suspend fun getCutoffTime(): CaffeineAnalysis           // 개인화 컷오프 시각 산출
    suspend fun getWeeklyInsight(
        averageSleepTime: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int
    ): String                                               // 주간 자연어 인사이트 생성

    suspend fun getTrendInsight(
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepTime: String
    ): String

    suspend fun getMonthlyInsight(
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepTime: String,
        highCaffeineDaySleepTime: String,
        lowCaffeineDaySleepTime: String
    ): String

    suspend fun getPeriodInsight(
        periodTotalCaffeine: Int,
        periodAvgCaffeine: Int,
        periodTotalSleepTime: String,
        periodAvgSleepTime: String
    ): String

    suspend fun getHomeInsight(
        recentDays: Int,
        beforeSleepHours: Int,
        lateCaffeineCount: Int,
        averageSleepTime: String,
        averageSleepScore: Int,
        bedtimeResidualCaffeineMg: Int
    ): String
}