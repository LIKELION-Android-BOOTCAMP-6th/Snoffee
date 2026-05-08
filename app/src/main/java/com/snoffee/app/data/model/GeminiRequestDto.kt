package com.snoffee.app.data.model

// Gemini API 요청 DTO
// Gemini API로 보내는 요청 데이터 형태
data class GeminiRequestDto(
    val weight: Double,                     // 체중 (kg)
    val sensitivity: Int,                   // 카페인 민감도 (1~3단계)
    val caffeineRecords: List<CaffeineRecordDto>, // 오늘 카페인 섭취 기록 목록
    val sleepData: SleepDataDto?            // 최근 수면 데이터 (없을 수 있음)
)