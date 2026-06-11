package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

class GetPeriodInsightUseCase @Inject constructor(
    private val repository: GeminiRepository
) {
    suspend operator fun invoke(
        periodTotalCaffeine: Int,
        periodAvgCaffeine: Int,
        periodTotalSleepTime: String,
        periodAvgSleepTime: String
    ): String {
        return repository.getPeriodInsight(
            periodTotalCaffeine = periodTotalCaffeine,
            periodAvgCaffeine = periodAvgCaffeine,
            periodTotalSleepTime = periodTotalSleepTime,
            periodAvgSleepTime = periodAvgSleepTime
        )
    }
}