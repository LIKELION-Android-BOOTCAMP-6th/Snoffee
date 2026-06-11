package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

class GetTrendInsightUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        monthlyCaffeineTrend: Map<String, Double>,
        monthlySleepTrend: Map<String, Double>,
        totalAvgSleepTime: String
    ): String {
        return repository.getTrendInsight(
            monthlyCaffeineTrend = monthlyCaffeineTrend,
            monthlySleepTrend = monthlySleepTrend,
            totalAvgSleepTime = totalAvgSleepTime
        )
    }
}