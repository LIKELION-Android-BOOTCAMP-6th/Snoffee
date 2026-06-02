package com.snoffee.app.domain.model

import java.time.LocalDate

// 카페인 영향도 레벨
enum class ImpactLevel {
    LOW,        // 카페인 영향 거의 없음
    MODERATE,   // 약간 영향
    HIGH        // 수면에 뚜렷한 영향
}

// 하루치 카페인-수면 상관관계 분석 결과
// Gemini 입력이자, Gemini 불가 시 멘트 생성의 입력으로 대체하게끔
data class DailyCorrelation(
    val date: LocalDate,

    // 카페인 집계
    val totalCaffeineMg: Double,            // 그날 총 섭취량
    val lateNightCaffeineCount: Int,        // 19시 이후 섭취 횟수
    val beforeSleepCaffeineCount: Int,      // 취침 3시간 이내 섭취 횟수
    val residualCaffeineAtSleep: Double,    // 취침 시점 체내 잔류량 mg

    // 수면 집계
    val sleepDurationMinutes: Long,         // 수면 시간
    val deepSleepRatio: Int,                // 깊은 수면 비율 (1~5, 클수록 좋음)

    // 분석 결과
    val impactScore: Double,                // 카페인의 수면 영향도
    val impactLevel: ImpactLevel,           // 영향도 레벨

    // 데이터 가용성
    val hasCaffeineData: Boolean,
    val hasSleepData: Boolean
)