package com.snoffee.app.domain.usecase.sleep

import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

// 삼성헬스 수면 데이터 조회 UseCase
// ViewModel에서 호출, SleepRepository를 통해 조회
class GetSleepDataUseCase @Inject constructor(
    private val repository: SleepRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(): SleepData? {
        // TODO: 최근 수면 데이터 조회
        return repository.getLatestSleepData()
    }
}