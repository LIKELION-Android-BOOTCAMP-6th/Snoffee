package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.util.CaffeineCalculator
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import javax.inject.Inject
import kotlin.math.roundToInt

// 현재 체내 카페인 잔류량 계산 UseCase
// CaffeineCalculator를 통해 반감기 기반 잔류량 계산
// HomeViewModel에서 5분 주기로 호출
class CalculateResidualUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,     // Hilt가 자동 주입
    private val userProfileRepository: UserProfileRepository, // Hilt가 자동 주입
    private val calculator: CaffeineCalculator              // Hilt가 자동 주입
) {
    suspend operator fun invoke(): Double {
        records: List<CaffeineRecord>,
        sensitivity: CaffeineSensitivity,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): Int {
        return records.sumOf { record ->
            CaffeineCalculator.calculateResidualCaffeine(
                intakeCaffeine = record.intakeCaffeine,
                consumedAt = record.consumedAt,
                currentTimeMillis = currentTimeMillis,
                halfLifeHours = sensitivity.halfLifeHours
            )
        }.roundToInt()
    }
}