package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

// 주간 카페인-수면 상관관계 리포트 조회 UseCase
// ReportViewModel에서 호출
// CaffeineRepository, SleepRepository를 통해 주간 데이터 조회
class GetWeeklyReportUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,  // Hilt가 자동 주입
    private val sleepRepository: SleepRepository          // Hilt가 자동 주입
) {
    suspend operator fun invoke(): Pair<List<CaffeineRecord>, SleepData?> {
        // TODO: 주간 카페인 기록 + 수면 데이터 조회 후 반환
        return Pair(emptyList(), null)
    }
}