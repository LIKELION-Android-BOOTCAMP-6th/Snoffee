package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSleepCorrelation
import com.snoffee.app.domain.util.CaffeineCalculator
import java.util.Calendar
import javax.inject.Inject

class AnalyzeCaffeineSleepCorrelationUseCase @Inject constructor(
    private val calculator: CaffeineCalculator
) {
    // 상수로 일부 수치 관리 (내부에서만 사용가능하게끔)
    companion object {
        private const val HALF_LIFE_HOURS = 5.0     // 카페인 반감기 시간
        private const val LATE_NIGHT_HOUR = 19      // 심야 시간 기준 (19시)
        private const val BEFORE_SLEEP_HOURS = 3    // 수면 전 제한 시간 (수면에 들기 3시간 전)
    }

    fun execute(
        records: List<CaffeineRecord>,          // 카페인 섭취 기록
        sleepStartTime: Long                    // 사용자 잠들기 시작한 시간
    ): CaffeineSleepCorrelation {
        if (records.isEmpty()) {                // 카페인 섭취 기록이 없으면 계산할 필요 없음
            return CaffeineSleepCorrelation(
                lateNightCaffeineCount = 0,
                beforeSleepCaffeineCount = 0,
                residualCaffeineAtSleep = 0.0
            )
        }

        return CaffeineSleepCorrelation(
            lateNightCaffeineCount = countLateNightCaffeine(records),                       // 19시 이후에 섭취한 카페인 횟수
            beforeSleepCaffeineCount = countBeforeSleepCaffeine(
                records,
                sleepStartTime
            ),   // 취침 전 제한 시간 이내에 섭취한 횟수
            residualCaffeineAtSleep = calculateResidualAtSleep(
                records,
                sleepStartTime
            )     // 반감기 활용하여 잠들기 전 카페인 잔량 계산
        )
    }

    // 19:00 이후 섭취 횟수
    private fun countLateNightCaffeine(records: List<CaffeineRecord>): Int {
        return records.count { record ->
            val hour = Calendar.getInstance().apply {
                timeInMillis = record.consumedAt
            }.get(Calendar.HOUR_OF_DAY)
            hour >= LATE_NIGHT_HOUR
        }
    }

    // 취침 3시간 이내 섭취 횟수
    private fun countBeforeSleepCaffeine(
        records: List<CaffeineRecord>,
        sleepStartTime: Long
    ): Int {
        val threeHoursMillis = BEFORE_SLEEP_HOURS * 60 * 60 * 1000L
        return records.count { record ->
            record.consumedAt in (sleepStartTime - threeHoursMillis)..sleepStartTime
        }
    }

    // 취침 시점 체내 잔류량 합산
    private fun calculateResidualAtSleep(
        records: List<CaffeineRecord>,
        sleepStartTime: Long
    ): Double {
        return records
            .filter { it.consumedAt <= sleepStartTime }     // 잠들기 전에 마신 기록만 filtering 하기
            .sumOf { record ->                              // 필터링된 각 기록의 잔류량을 각각 계산
                calculator.calculateResidualCaffeine(
                    intakeCaffeine = record.intakeCaffeine,
                    consumedAt = record.consumedAt,
                    currentTimeMillis = sleepStartTime,
                    halfLifeHours = HALF_LIFE_HOURS
                )
            }
            .coerceAtLeast(0.0) // 음수 방어
    }
}