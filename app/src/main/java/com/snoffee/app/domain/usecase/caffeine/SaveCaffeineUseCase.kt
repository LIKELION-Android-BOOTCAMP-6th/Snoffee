package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import javax.inject.Inject

// 카페인 섭취 기록 저장 UseCase
// ViewModel에서 호출, CaffeineRepository를 통해 저장
class SaveCaffeineUseCase @Inject constructor(
    private val repository: CaffeineRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(record: CaffeineRecord) {
        // TODO: 유효성 검사 후 저장
        repository.saveCaffeineRecord(record)
    }
}