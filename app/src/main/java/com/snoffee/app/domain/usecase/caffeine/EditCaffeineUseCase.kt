package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import javax.inject.Inject

class EditCaffeineUseCase @Inject constructor(
    private val repository: CaffeineRepository  // Hilt가 자동 주입
) {
    suspend operator fun invoke(record: CaffeineRecord) {
        repository.editCaffeineRecord(record)
    }
}