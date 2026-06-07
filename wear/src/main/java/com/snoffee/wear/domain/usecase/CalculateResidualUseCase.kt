package com.snoffee.wear.domain.usecase

import com.snoffee.wear.core.caffeine.CaffeineCalculator
import com.snoffee.wear.domain.model.CaffeineAnalysis
import com.snoffee.wear.domain.model.CaffeineRecord
import javax.inject.Inject

class CalculateResidualUseCase @Inject constructor(
    private val calculator: CaffeineCalculator
) {
    operator fun invoke(
        records: List<CaffeineRecord>,
        halfLifeHours: Double,
        now: Long
    ): CaffeineAnalysis {

        val totalResidual = records.sumOf { record ->
            calculator.calculateResidualCaffeine(
                intakeCaffeine = record.amount,
                consumedAt = record.time,
                currentTimeMillis = now,
                halfLifeHours = halfLifeHours
            )
        }

        return CaffeineAnalysis(
            residualAmount = totalResidual,
            cutoffTime = calculator.calculateZeroCaffeineTime(
                totalResidual,
                records.firstOrNull()?.time ?: now,
                halfLifeHours
            ),
            sleepImpactScore = if (totalResidual > 50.0) 0.7 else 0.2
        )
    }
}