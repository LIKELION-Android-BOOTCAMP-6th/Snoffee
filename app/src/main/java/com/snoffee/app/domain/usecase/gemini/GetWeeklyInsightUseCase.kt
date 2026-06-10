package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

// Gemini API로 개인화 컷오프 시간 산출 UseCase
// ViewModel에서 호출, GeminiRepository를 통해 Gemini API 호출
class GetWeeklyInsightUseCase @Inject constructor(
    private val repository: GeminiRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(
        averageSleepTime: String,
        lateCaffeineCount: Int,
        bedtimeResidualCaffeineMg: Int
    ): String {

        return repository.getWeeklyInsight(
            averageSleepTime,
            lateCaffeineCount,
            bedtimeResidualCaffeineMg
        )
    }
}