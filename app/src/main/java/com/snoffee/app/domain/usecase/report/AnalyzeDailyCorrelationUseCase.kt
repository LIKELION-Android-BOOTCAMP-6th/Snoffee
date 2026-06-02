package com.snoffee.app.domain.usecase.report

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSleepReport
import com.snoffee.app.domain.model.DailyCorrelation
import com.snoffee.app.domain.model.ImpactLevel
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.SleepSource
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// 카페인 기록과 수면 데이터를 날짜별로 매칭하여 상관관계를 분석한다.

/*
* 내장 되어있는 기능
* - 날짜별 CaffeineRecord와 SleepData 매칭
* - 날짜별 총 카페인량 / 수면 시간 / 깊은 수면 비율 집계
* - 카페인 분석 알고리즘을 날짜별로 재사용하여 잔류량·횟수 산출
* - 카페인 영향도 계산 및 최대 영향 날짜 추출
*/

// 분석 결과는 Gemini 입력이자, Gemini 불가 시 수동 인사이트가 됨
class AnalyzeDailyCorrelationUseCase @Inject constructor(
    private val analyzeCaffeineSleep: AnalyzeCaffeineSleepCorrelationUseCase
) {
    companion object {
        // 영향도 가중치 (합 = 1.0)
        private const val WEIGHT_RESIDUAL = 0.5      // 취침 시점 잔류량
        private const val WEIGHT_BEFORE_SLEEP = 0.3  // 취침 3시간 이내 섭취
        private const val WEIGHT_DEEP_SLEEP = 0.2    // 깊은 수면 부족

        // 정규화 기준치
        private const val RESIDUAL_REFERENCE_MG = 100.0  // 잔류량 100mg을 영향도 1.0 기준으로
        private const val BEFORE_SLEEP_REFERENCE = 2.0   // 취침 전 2회 섭취를 1.0 기준으로

        // 영향도 레벨 경계값
        private const val THRESHOLD_HIGH = 0.6
        private const val THRESHOLD_MODERATE = 0.3

        // 수면 시간 유효 범위 (분) — 비정상 데이터 가드
        private const val MIN_VALID_SLEEP_MINUTES = 60L        // 1시간 미만은 비정상
        private const val MAX_VALID_SLEEP_MINUTES = 18 * 60L   // 18시간 초과는 비정상

        // 깊은 수면 비율 유효 범위
        private const val MIN_DEEP_SLEEP = 1
        private const val MAX_DEEP_SLEEP = 5
    }

    fun execute(
        caffeineRecords: List<CaffeineRecord>,
        sleepDataList: List<SleepData>,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): CaffeineSleepReport {
        // 날짜별 카페인 섭취 기록
        val caffeineByDate: Map<LocalDate, List<CaffeineRecord>> =
            caffeineRecords.groupBy { it.consumedAt.toLocalDate(zoneId) }

        // 수면은 레포지토리(deduplicateByDate)에서 이미 날짜당 1건으로 정리된거 사용
        val sleepByDate: Map<LocalDate, SleepData> =
            sleepDataList
                .groupBy { it.date.toLocalDate(zoneId) }
                .mapValues { (_, list) ->
                    list.maxWith(
                        compareBy<SleepData> { sleep ->
                            when (sleep.source) {
                                SleepSource.MANUAL -> 2
                                SleepSource.SAMSUNG_HEALTH -> 1
                            }
                        }.thenBy { sleep -> sleep.sleepEnd }
                    )
                }

        // 카페인, 수면 둘 다 있는 날짜만 상관관계 분석 하게끔 하기
        val analyzableDates: Set<LocalDate> = caffeineByDate.keys intersect sleepByDate.keys

        val correlations = analyzableDates
            .sorted()
            .mapNotNull { date ->
                val records = caffeineByDate[date] ?: return@mapNotNull null
                val sleep = sleepByDate[date] ?: return@mapNotNull null
                buildDailyCorrelation(date, records, sleep)
            }

        if (correlations.isEmpty()) {
            return CaffeineSleepReport.empty()
        }

        val worstDay = correlations.maxByOrNull { it.impactScore }

        return CaffeineSleepReport(
            dailyCorrelations = correlations,
            worstDay = worstDay,
            isSufficientData = true
        )
    }

    private fun buildDailyCorrelation(
        date: LocalDate,
        records: List<CaffeineRecord>,
        sleep: SleepData
    ): DailyCorrelation? {
        // 수면 데이터 검증 (sleepStart > sleepEnd, 비정상 길이 가드)
        val durationMillis = sleep.sleepEnd - sleep.sleepStart
        if (durationMillis <= 0) {      // 수면 시작이 종료보다 늦음 → 해당 날짜 분석 제외
            return null
        }
        val sleepDurationMinutes = durationMillis / (60 * 1000L)
        if (sleepDurationMinutes < MIN_VALID_SLEEP_MINUTES ||
            sleepDurationMinutes > MAX_VALID_SLEEP_MINUTES
        ) {
            // 비정상적으로 짧거나 긴 수면 → 분석 제외
            return null
        }

        val deepSleepRatio = sleep.deepSleepRatio.coerceIn(MIN_DEEP_SLEEP, MAX_DEEP_SLEEP)

        // 잔류량·횟수 계산
        val correlation = analyzeCaffeineSleep.execute(
            records = records,
            sleepStartTime = sleep.sleepStart
        )

        val totalCaffeineMg = records.sumOf { it.intakeCaffeine }.coerceAtLeast(0.0)

        // 영향도 계산
        val impactScore = calculateImpactScore(
            residualMg = correlation.residualCaffeineAtSleep,
            beforeSleepCount = correlation.beforeSleepCaffeineCount,
            deepSleepRatio = deepSleepRatio
        )

        return DailyCorrelation(
            date = date,
            totalCaffeineMg = totalCaffeineMg,
            lateNightCaffeineCount = correlation.lateNightCaffeineCount,
            beforeSleepCaffeineCount = correlation.beforeSleepCaffeineCount,
            residualCaffeineAtSleep = correlation.residualCaffeineAtSleep,
            sleepDurationMinutes = sleepDurationMinutes,
            deepSleepRatio = deepSleepRatio,
            impactScore = impactScore,
            impactLevel = toImpactLevel(impactScore),
            hasCaffeineData = records.isNotEmpty(),
            hasSleepData = true
        )
    }

    // 카페인의 수면 영향도 (0.0 ~ 1.0).
    // 잔류량 높음 + 취침 직전 섭취 많음 + 깊은 수면 부족 → 영향도 높음.
    private fun calculateImpactScore(
        residualMg: Double,
        beforeSleepCount: Int,
        deepSleepRatio: Int
    ): Double {
        // 각 요소를 0~1로 정규화
        val residualFactor = (residualMg / RESIDUAL_REFERENCE_MG).coerceIn(0.0, 1.0)
        val beforeSleepFactor = (beforeSleepCount / BEFORE_SLEEP_REFERENCE).coerceIn(0.0, 1.0)
        // 깊은 수면이 적을수록(1에 가까울수록) 영향 큼 → 역방향 정규화
        val deepSleepFactor = ((MAX_DEEP_SLEEP - deepSleepRatio).toDouble() /
                (MAX_DEEP_SLEEP - MIN_DEEP_SLEEP)).coerceIn(0.0, 1.0)

        val score = WEIGHT_RESIDUAL * residualFactor +
                WEIGHT_BEFORE_SLEEP * beforeSleepFactor +
                WEIGHT_DEEP_SLEEP * deepSleepFactor

        return score.coerceIn(0.0, 1.0)
    }

    private fun toImpactLevel(score: Double): ImpactLevel = when {
        score >= THRESHOLD_HIGH -> ImpactLevel.HIGH
        score >= THRESHOLD_MODERATE -> ImpactLevel.MODERATE
        else -> ImpactLevel.LOW
    }

    private fun Long.toLocalDate(zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate()
}