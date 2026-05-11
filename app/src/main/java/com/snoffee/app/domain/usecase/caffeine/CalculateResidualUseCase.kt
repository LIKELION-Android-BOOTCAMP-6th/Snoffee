package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.util.CaffeineCalculator
import javax.inject.Inject
import kotlin.math.roundToInt

class CalculateResidualUseCase @Inject constructor() {
    operator fun invoke(
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