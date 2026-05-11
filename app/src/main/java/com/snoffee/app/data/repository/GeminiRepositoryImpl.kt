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

    override suspend fun getWeeklyInsight(): String {
        // TODO: Gemini API 호출 후 인사이트 문자열 반환
        TODO("Not yet implemented")
    }
}