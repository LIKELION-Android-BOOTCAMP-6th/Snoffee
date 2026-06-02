package com.snoffee.app.domain.model

// 카페인-수면 상관관계 전체 리포트
// 분석 기간 전체의 날짜별 결과를 담는다.
data class CaffeineSleepReport(
    val dailyCorrelations: List<DailyCorrelation>,  // 날짜별 분석 결과 (분석 가능한 날짜만)
    val worstDay: DailyCorrelation?,                // 카페인 영향이 가장 큰 날짜 (없으면 null)
    val isSufficientData: Boolean                   // 분석 가능한 데이터가 충분한지 (fallback 판단용)
) {
    companion object {
        // 분석 가능한 데이터가 부족할 때의 빈 리포트
        fun empty(): CaffeineSleepReport = CaffeineSleepReport(
            dailyCorrelations = emptyList(),
            worstDay = null,
            isSufficientData = false
        )
    }
}