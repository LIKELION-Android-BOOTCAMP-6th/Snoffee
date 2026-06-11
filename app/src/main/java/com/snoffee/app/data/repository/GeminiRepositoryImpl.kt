package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.remote.GeminiRemoteDataSource
import com.snoffee.app.data.mapper.GeminiMapper
import com.snoffee.app.domain.model.CaffeineAnalysis
import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

// GeminiRepository 인터페이스 구현체
// GeminiRemoteDataSource를 통해 Gemini API 호출
// GeminiMapper를 통해 DTO ↔ Domain Model 변환
class GeminiRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeminiRemoteDataSource,  // Hilt가 자동 주입
    private val mapper: GeminiMapper                       // Hilt가 자동 주입
) : GeminiRepository {

    override suspend fun getCutoffTime(): CaffeineAnalysis {
        // TODO: Gemini API 호출 후 Domain Model로 변환해서 반환
        TODO("Not yet implemented")
    }

    override suspend fun getWeeklyInsight(
        averageSleepTime: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {

        return remoteDataSource.generateWeeklyInsight(
            averageSleepTime,
            lateCaffeineCount,
            bedtimeResidualCaffeineMg
        )
    }

    override suspend fun getTrendInsight(
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepTime: String
    ): String {
        return remoteDataSource.generateTrendInsight(
            monthlyCaffeineTrend = monthlyCaffeineTrend,
            monthlySleepTrend = monthlySleepTrend,
            totalAvgSleepTime = totalAvgSleepTime
        )
    }

    override suspend fun getMonthlyInsight(
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepTime: String,
        highCaffeineDaySleepTime: String,
        lowCaffeineDaySleepTime: String
    ): String {
        return remoteDataSource.generateMonthlyInsight(
            monthlyAvgCaffeine = monthlyAvgCaffeine,
            monthlyAvgSleepTime = monthlyAvgSleepTime,
            highCaffeineDaySleepTime = highCaffeineDaySleepTime,
            lowCaffeineDaySleepTime = lowCaffeineDaySleepTime
        )
    }

    override suspend fun getPeriodInsight(
        periodTotalCaffeine: Int,
        periodAvgCaffeine: Int,
        periodTotalSleepTime: String,
        periodAvgSleepTime: String
    ): String {
        return remoteDataSource.generatePeriodInsight(
            periodTotalCaffeine = periodTotalCaffeine,
            periodAvgCaffeine = periodAvgCaffeine,
            periodTotalSleepTime = periodTotalSleepTime,
            periodAvgSleepTime = periodAvgSleepTime
        )
    }

    override suspend fun getHomeInsight(
        recentDays: Int,
        beforeSleepHours: Int,
        lateCaffeineCount: Int,
        averageSleepTime: String,
        averageSleepScore: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {
        return remoteDataSource.generateHomeInsight(
            recentDays = recentDays,
            beforeSleepHours = beforeSleepHours,
            lateCaffeineCount = lateCaffeineCount,
            averageSleepTime = averageSleepTime,
            averageSleepScore = averageSleepScore,
            bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
        )
    }
}