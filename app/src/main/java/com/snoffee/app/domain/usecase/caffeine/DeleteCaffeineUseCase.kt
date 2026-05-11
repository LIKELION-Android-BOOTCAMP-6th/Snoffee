package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.repository.CaffeineRepository
import javax.inject.Inject

// 카페인 섭취 기록 삭제 UseCase
// ViewModel에서 호출, CaffeineRepository를 통해 삭제
class DeleteCaffeineUseCase @Inject constructor(
    private val repository: CaffeineRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteCaffeineRecord(id)
    }
}