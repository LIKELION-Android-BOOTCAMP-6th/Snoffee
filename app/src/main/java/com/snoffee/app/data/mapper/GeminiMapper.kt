package com.snoffee.app.data.mapper

import com.snoffee.app.data.model.GeminiResponseDto
import com.snoffee.app.domain.model.CaffeineAnalysis
import javax.inject.Inject

// GeminiResponseDto ↔ CaffeineAnalysis 변환
class GeminiMapper @Inject constructor() {

    fun toDomain(dto: GeminiResponseDto): CaffeineAnalysis {
        // TODO: DTO → Domain Model 변환
        TODO("Not yet implemented")
    }
}