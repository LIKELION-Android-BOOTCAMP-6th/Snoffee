package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

class GetMonthlyInsightUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        monthlyAvgCaffeine: Int,
        monthlyAvgSleepTime: String,
        highCaffeineDaySleepTime: String,
        lowCaffeineDaySleepTime: String
    ): String {
        return repository.getMonthlyInsight(
            monthlyAvgCaffeine = monthlyAvgCaffeine,
            monthlyAvgSleepTime = monthlyAvgSleepTime,
            highCaffeineDaySleepTime = highCaffeineDaySleepTime,
            lowCaffeineDaySleepTime = lowCaffeineDaySleepTime
        )
    }
}