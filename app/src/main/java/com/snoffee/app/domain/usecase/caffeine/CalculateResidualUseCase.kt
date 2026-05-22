package com.snoffee.app.domain.usecase.caffeine

import com.snoffee.app.domain.model.CaffeineAnalysis
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import com.snoffee.app.domain.util.CaffeineCalculator
import javax.inject.Inject
import kotlin.math.log2
class CalculateResidualUseCase @Inject constructor(
    private val caffeineRepository: CaffeineRepository,
    private val userProfileRepository: UserProfileRepository,
    private val calculator: CaffeineCalculator
) {
    suspend operator fun invoke(): CaffeineAnalysis {
        val now = System.currentTimeMillis()

        val fiveDaysAgo = now - (5L * 24 * 60 * 60 * 1000)

        val records =
            caffeineRepository.getCaffeineRecordsSince(fiveDaysAgo)
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
        val targetMinCaffeine = 10.0

        val totalResidual = records.sumOf { record ->
            val safeConsumedAt = if (record.consumedAt > now) now else record.consumedAt

            val residual = calculator.calculateResidualCaffeine(
                intakeCaffeine = record.intakeCaffeine,
                consumedAt = safeConsumedAt,
                currentTimeMillis = now,
                halfLifeHours = halfLifeHours
            )

            if (residual < targetMinCaffeine) 0.0 else residual
        }

        //모든 음료 중 가장 늦게 대사가 끝나는 10mg 시각 찾기
        val finalZeroTime = if (totalResidual <= targetMinCaffeine) {
            now
        } else {
            // 현재 총 잔류량 다 사라지는 데 걸리는 시간 역산
            val requiredHours = halfLifeHours * log2(totalResidual / targetMinCaffeine)
            val requiredMillis = (requiredHours * 60 * 60 * 1000).toLong()

            now + requiredMillis
        }
        //분석 결과 모델로 리턴
        return CaffeineAnalysis(
            residualAmount = totalResidual,
            cutoffTime = finalZeroTime,
            sleepImpactScore = if (totalResidual > 50.0) 0.7 else 0.2
        )
    }
}