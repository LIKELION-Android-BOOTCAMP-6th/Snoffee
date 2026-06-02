package com.snoffee.app.usecase

import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.SleepSource
import com.snoffee.app.domain.usecase.report.AnalyzeCaffeineSleepCorrelationUseCase
import com.snoffee.app.domain.usecase.report.AnalyzeDailyCorrelationUseCase
import com.snoffee.app.domain.usecase.report.GenerateFallbackInsightUseCase
import com.snoffee.app.domain.util.CaffeineCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * B-6-2 통합 테스트
 * 6-1(AnalyzeCaffeineSleepCorrelationUseCase) + 실제 CaffeineCalculator를 엮어
 * 날짜 매칭 → 집계 → 영향도 → fallback 멘트까지 검증한다.
 */
class AnalyzeDailyCorrelationUseCaseTest {

    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    private lateinit var analyzeDaily: AnalyzeDailyCorrelationUseCase
    private lateinit var fallback: GenerateFallbackInsightUseCase

    @Before
    fun setUp() {
        val calculator = CaffeineCalculator()
        val sixOne = AnalyzeCaffeineSleepCorrelationUseCase(calculator)
        analyzeDaily = AnalyzeDailyCorrelationUseCase(sixOne)
        fallback = GenerateFallbackInsightUseCase()
    }

    // 시간 헬퍼
    private fun epoch(date: LocalDate, hour: Int, minute: Int = 0): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun caffeine(date: LocalDate, hour: Int, mg: Double, minute: Int = 0) =
        CaffeineRecord(
            drinkId = "drink-test",
            drinkName = "테스트 음료",
            brandName = "테스트 브랜드",
            intakeSize = 355.0,
            intakeCaffeine = mg,
            consumedAt = epoch(date, hour, minute)
        )

    private fun sleep(
        date: LocalDate,
        startHour: Int,
        durationHours: Int,
        deepSleepRatio: Int,
        startMinute: Int = 0,
        durationMinutes: Int = 0,
        source: SleepSource = SleepSource.SAMSUNG_HEALTH
    ): SleepData {
        val start = epoch(date, startHour, startMinute)
        val end = start + (durationHours * 60L + durationMinutes) * 60 * 1000L
        return SleepData(
            date = epoch(date, 0),
            sleepStart = start,
            sleepEnd = end,
            deepSleepRatio = deepSleepRatio,
            source = source
        )
    }

    @Test
    fun `날짜별 카페인-수면 매칭 및 집계 성공`() {
        val d = LocalDate.of(2026, 5, 1)
        val records = listOf(
            caffeine(d, 9, 100.0),
            caffeine(d, 21, 80.0)   // 늦은 시간
        )
        val sleeps = listOf(sleep(d, 23, 7, deepSleepRatio = 3))

        val report = analyzeDaily.execute(records, sleeps, zone)

        assertTrue(report.isSufficientData)
        assertEquals(1, report.dailyCorrelations.size)
        val day = report.dailyCorrelations.first()
        assertEquals(d, day.date)
        assertEquals(180.0, day.totalCaffeineMg, 0.01)
        assertEquals(1, day.lateNightCaffeineCount)        // 21시 1건
        assertEquals(7 * 60L, day.sleepDurationMinutes)    // 7시간
    }

    @Test
    fun `카페인 영향 큰 날짜가 worstDay로 추출됨`() {
        val good = LocalDate.of(2026, 5, 1)
        val bad = LocalDate.of(2026, 5, 2)

        val records = listOf(
            // 좋은 날: 아침 커피만
            caffeine(good, 8, 80.0),
            // 나쁜 날: 취침 직전 다량
            caffeine(bad, 22, 150.0),
            caffeine(bad, 23, 100.0)
        )
        val sleeps = listOf(
            sleep(good, 23, 8, deepSleepRatio = 5),
            // 나쁜 날: 깊은 수면 비율 최저(1), 6시간 수면
            sleep(bad, 0, 6, deepSleepRatio = 1)
        )

        val report = analyzeDaily.execute(records, sleeps, zone)

        assertNotNull(report.worstDay)
        assertEquals(bad, report.worstDay!!.date)
        assertTrue(report.worstDay!!.impactScore > 0.0)
    }

    @Test
    fun `카페인 기록이 없으면 분석 불가`() {
        val d = LocalDate.of(2026, 5, 1)
        val report = analyzeDaily.execute(
            caffeineRecords = emptyList(),
            sleepDataList = listOf(sleep(d, 23, 7, deepSleepRatio = 3)),
            zoneId = zone
        )
        assertFalse(report.isSufficientData)
        assertTrue(report.dailyCorrelations.isEmpty())
        assertNull(report.worstDay)
    }

    @Test
    fun `수면 데이터가 없으면 분석 불가`() {
        val d = LocalDate.of(2026, 5, 1)
        val report = analyzeDaily.execute(
            caffeineRecords = listOf(caffeine(d, 21, 100.0)),
            sleepDataList = emptyList(),
            zoneId = zone
        )
        assertFalse(report.isSufficientData)
    }

    @Test
    fun `카페인만 있는 날짜는 분석에서 제외`() {
        val onlyCaffeine = LocalDate.of(2026, 5, 1)
        val both = LocalDate.of(2026, 5, 2)

        val records = listOf(
            caffeine(onlyCaffeine, 21, 100.0),
            caffeine(both, 9, 80.0)
        )
        val sleeps = listOf(sleep(both, 23, 7, deepSleepRatio = 4))

        val report = analyzeDaily.execute(records, sleeps, zone)
        assertEquals(1, report.dailyCorrelations.size)
        assertEquals(both, report.dailyCorrelations.first().date)
    }

    @Test
    fun `수면 시작이 종료보다 늦으면 해당 날짜 제외`() {
        val d = LocalDate.of(2026, 5, 1)
        val start = epoch(d, 23)
        val brokenSleep = SleepData(
            date = epoch(d, 0),
            sleepStart = start,
            sleepEnd = start - 1000L,   // 종료가 시작보다 이전
            deepSleepRatio = 3,
            source = SleepSource.MANUAL
        )
        val report = analyzeDaily.execute(
            caffeineRecords = listOf(caffeine(d, 21, 100.0)),
            sleepDataList = listOf(brokenSleep),
            zoneId = zone
        )
        assertFalse(report.isSufficientData)
    }

    @Test
    fun `비정상적으로 짧은 수면은 제외`() {
        val d = LocalDate.of(2026, 5, 1)
        val report = analyzeDaily.execute(
            caffeineRecords = listOf(caffeine(d, 21, 100.0)),
            sleepDataList = listOf(sleep(d, 23, durationHours = 0, deepSleepRatio = 3)),
            zoneId = zone
        )
        // 0시간 수면 → 제외
        assertFalse(report.isSufficientData)
    }

    @Test
    fun `같은 날 중복 수면 데이터는 MANUAL을 우선 선택`() {
        val d = LocalDate.of(2026, 5, 1)
        val records = listOf(caffeine(d, 9, 80.0))
        // 같은 날 두 건: HealthConnect 6시간(ratio=2) vs 수동입력 7시간(ratio=4)
        // 레포가 이미 dedup 하지만, 분석도 동일 우선순위(MANUAL 우선)로 1건 선택해야 함
        val sleeps = listOf(
            sleep(
                d,
                23,
                durationHours = 6,
                deepSleepRatio = 2,
                source = SleepSource.SAMSUNG_HEALTH
            ),
            sleep(d, 23, durationHours = 7, deepSleepRatio = 4, source = SleepSource.MANUAL)
        )

        val report = analyzeDaily.execute(records, sleeps, zone)

        assertEquals(1, report.dailyCorrelations.size)
        val day = report.dailyCorrelations.first()
        // MANUAL(7시간, ratio=4)이 선택되어야 함
        assertEquals(7 * 60L, day.sleepDurationMinutes)
        assertEquals(4, day.deepSleepRatio)
    }

    @Test
    fun `fallback 멘트 - 데이터 부족 시 안내 문구`() {
        val empty = analyzeDaily.execute(emptyList(), emptyList(), zone)
        val msg = fallback.execute(empty)
        assertTrue(msg.contains("충분하지 않음"))
    }

    @Test
    fun `fallback 멘트 - HIGH 영향 시 공감 문구 포함`() {
        val bad = LocalDate.of(2026, 5, 2)
        val records = listOf(
            caffeine(bad, 22, 200.0),
            caffeine(bad, 23, 150.0)
        )
        val sleeps = listOf(sleep(bad, 23, 5, deepSleepRatio = 1, startMinute = 30))

        val report = analyzeDaily.execute(records, sleeps, zone)
        val msg = fallback.execute(report)

        assertNotNull(report.worstDay)
        // 영향이 큰 케이스이므로 mg 수치가 멘트에 포함되어야 함
        assertTrue(msg.contains("mg"))
    }
}