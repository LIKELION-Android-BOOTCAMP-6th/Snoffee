package com.snoffee.app.domain.usecase.sleep

import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.repository.SleepRepository
import javax.inject.Inject

class DeleteSleepDataUseCase @Inject constructor(
    private val repository: SleepRepository
) {
    suspend operator fun invoke(sleepData: SleepData): Result<Unit> = runCatching {
        repository.saveSleepData(sleepData.copy(deepSleepRatio = 0))
    }
}