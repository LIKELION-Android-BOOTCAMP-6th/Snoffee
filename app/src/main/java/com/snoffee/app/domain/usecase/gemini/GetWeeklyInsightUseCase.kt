package com.snoffee.app.domain.usecase.gemini

import com.snoffee.app.domain.model.CaffeineAnalysis
import com.snoffee.app.domain.repository.GeminiRepository
import javax.inject.Inject

// Gemini API로 개인화 컷오프 시간 산출 UseCase
// ViewModel에서 호출, GeminiRepository를 통해 Gemini API 호출
class GetWeeklyInsightUseCase @Inject constructor(
    private val repository: GeminiRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(): String {
        // TODO: Gemini API 호출 후 인사이트 문자열 반환
        return repository.getWeeklyInsight()
    }
}