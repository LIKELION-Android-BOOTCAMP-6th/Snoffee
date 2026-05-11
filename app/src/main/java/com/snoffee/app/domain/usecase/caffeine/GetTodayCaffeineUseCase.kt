package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import javax.inject.Inject

// 오늘 카페인 섭취 목록 조회 UseCase
// ViewModel에서 호출, CaffeineRepository를 통해 조회
class GetTodayCaffeineUseCase @Inject constructor(
    private val repository: CaffeineRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(): List<CaffeineRecord> {
        // TODO: 오늘 날짜 기준으로 필터링
        return repository.getTodayCaffeineRecords()
    }
}