package com.snoffee.app.data.datasource.remote

/**
 * Gemini API 접근 계약 (인터페이스)
 * 실제 구현은 GeminiRemoteDataSourceImpl에서
 */
interface GeminiRemoteDataSource {

    suspend fun generateWeeklyInsight(
        averageSleepTime: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int
    ): String

    suspend fun generateTrendInsight(
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepTime: String
    ): String

    suspend fun generateMonthlyInsight(
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepTime: String,
        highCaffeineDaySleepTime: String,
        lowCaffeineDaySleepTime: String
    ): String

    suspend fun generatePeriodInsight(
        periodTotalCaffeine: Int,
        periodAvgCaffeine: Int,
        periodTotalSleepTime: String,
        periodAvgSleepTime: String
    ): String

    suspend fun generateHomeInsight(
        recentDays: Int,
        beforeSleepHours: Int,
        lateCaffeineCount: Int,
        averageSleepTime: String,
        averageSleepScore: Int,
        bedtimeResidualCaffeineMg: Int
    ): String
}