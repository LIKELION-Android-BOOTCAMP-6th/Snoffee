package com.snoffee.app.domain.model

import java.time.LocalDateTime

// 사용자 프로필
// 온보딩에서 입력한 사용자 설정값.
data class UserProfile(
    val id: Int = 1,                        // PK, 항상 1 (단일 사용자)
    val weight: Double,                     // 체중 (카페인 대사율 보정에 사용)
    val dailyCaffeineLimit: Double,         // 하루 카페인 권장량 (mg)
    val onboardingCompleted: Boolean = false, // 온보딩 완료 여부
    val userSleepTime: LocalDateTime,       // 목표 수면 시각
    val wakeTime: LocalDateTime,            // 기상 시각
    val sensitivity: Int,                   // 카페인 민감도 (1~3단계, Gemini 파라미터)
    val cutoffTime: LocalDateTime?          // 마지막으로 산출된 컷오프 시각 (캐싱용, 없을 수 있음)
)
enum class CaffeineSensitivity(
    val halfLifeHours: Double
) {
    SENSITIVE(6.0), // 민감
    NORMAL(5.0), // 적당
    LOW(4.0) // 낮음
}