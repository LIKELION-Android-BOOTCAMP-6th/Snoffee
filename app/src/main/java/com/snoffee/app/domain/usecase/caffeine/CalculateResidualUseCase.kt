package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.util.CaffeineCalculator
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateResidualUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,
    private val userProfileRepository: UserProfileRepository,
    private val calculator: CaffeineCalculator
) {
    suspend operator fun invoke(): Int {
        val records =
            caffeineRepository.getTodayCaffeineRecords()
        val userProfile =
            userProfileRepository.getUserProfile()
        val halfLifeHours = when (
            userProfile?.sensitivity
        ) {
            1 -> 6.0 // 민감
            2 -> 5.0 // 적당
            3 -> 4.0 // 낮음
            else -> 5.0
        }
        return records.sumOf { record ->
            calculator.calculateResidualCaffeine(
                intakeCaffeine = record.intakeCaffeine,
                consumedAt = record.consumedAt,
                halfLifeHours = halfLifeHours
            )
        }.roundToInt()
    }
}