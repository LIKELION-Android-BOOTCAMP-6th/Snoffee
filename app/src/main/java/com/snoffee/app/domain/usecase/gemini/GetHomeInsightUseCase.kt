package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

class GetHomeInsightUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        recentDays: Int,
        beforeSleepHours: Int,
        lateCaffeineCount: Int,
        averageSleepTime: String,
        averageSleepScore: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {
        return repository.getHomeInsight(
            recentDays = recentDays,
            beforeSleepHours = beforeSleepHours,
            lateCaffeineCount = lateCaffeineCount,
            averageSleepTime = averageSleepTime,
            averageSleepScore = averageSleepScore,
            bedtimeResidualCaffeineMg = bedtimeResidualCaffeineMg
        )
    }
}