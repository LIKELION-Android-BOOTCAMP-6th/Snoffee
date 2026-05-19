package com.snoffee.app.domain.usecase.sleep

import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

// 수면 데이터 저장 UseCase
// 워치 미착용 시 수동 입력 포함
// ViewModel에서 호출, SleepRepository를 통해 저장
class SaveSleepDataUseCase @Inject constructor(
    private val repository: SleepRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(sleepData: SleepData): Result<Unit> {
        // TODO: 유효성 검사 후 저장
        return repository.saveSleepData(sleepData)
    }
}