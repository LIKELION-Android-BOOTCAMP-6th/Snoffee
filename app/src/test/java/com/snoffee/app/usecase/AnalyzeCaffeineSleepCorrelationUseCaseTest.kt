package com.snoffee.app.usecase

import com.snoffee.app.core.caffeine.CaffeineCalculator
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.usecase.report.AnalyzeCaffeineSleepCorrelationUseCase
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar

// 유닛 테스트로 AnalyzeCaffeineSleepCorrelationUseCase 클래스 파일에 있는 로직이 잘 돌아가는지 확인 완료
class AnalyzeCaffeineSleepCorrelationUseCaseTest {

    private lateinit var useCase: AnalyzeCaffeineSleepCorrelationUseCase

    @Before
    fun setUp() {
        useCase = AnalyzeCaffeineSleepCorrelationUseCase(CaffeineCalculator())
    }

    // 헬퍼 함수 - 오늘 날짜 기준 특정 시각을 타임스탬프로 변환
    private fun toMillis(hour: Int, minute: Int): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun makeRecord(hour: Int, minute: Int, caffeine: Double): CaffeineRecord {
        return CaffeineRecord(
            drinkId = "test",
            drinkName = "테스트 음료",
            brandName = "테스트 브랜드",
            intakeSize = 300.0,
            intakeCaffeine = caffeine,
            consumedAt = toMillis(hour, minute)
        )
    }

    // 테스트 1: 19:00 이후 섭취만 카운팅
    @Test
    fun `19시 이후 섭취만 늦은 카페인으로 카운팅된다`() {
        val records = listOf(
            makeRecord(18, 0, 100.0),   // 미해당
            makeRecord(19, 0, 100.0),   // 해당
            makeRecord(21, 30, 100.0)   // 해당
        )

        val result = useCase.execute(records, sleepStartTime = toMillis(23, 0))

        assertEquals(2, result.lateNightCaffeineCount)
    }

    // 테스트 2: 취침 3시간 이내 섭취만 카운팅
    @Test
    fun `취침 3시간 이내 섭취만 카운팅된다`() {
        val sleepStart = toMillis(23, 0)
        val records = listOf(
            makeRecord(19, 30, 100.0),  // 3시간 초과
            makeRecord(20, 0, 100.0),   // 정확히 3시간
            makeRecord(21, 0, 100.0),   // 2시간 전
            makeRecord(22, 30, 100.0)   // 30분 전
        )

        val result = useCase.execute(records, sleepStartTime = sleepStart)

        assertEquals(3, result.beforeSleepCaffeineCount)
    }

    // 테스트 3: 반감기 공식 기반 잔류량 계산
    @Test
    fun `취침 시점 체내 잔류량이 반감기 공식으로 계산된다`() {
        val sleepStart = toMillis(23, 0)
        val records = listOf(
            makeRecord(18, 0, 100.0)  // 5시간 전 → 반감기 1회 → 50mg
        )

        val result = useCase.execute(records, sleepStartTime = sleepStart)

        assertEquals(50.0, result.residualCaffeineAtSleep, 0.1) // 오차 0.1mg 허용
    }

    // 테스트 4: 자정을 넘기는 케이스
    @Test
    fun `자정을 넘기는 경우에도 타임스탬프 기반으로 정확히 판별된다`() {
        val nextDay = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val sleepStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val records = listOf(
            CaffeineRecord(
                drinkId = "test",
                drinkName = "테스트 음료",
                brandName = "테스트 브랜드",
                intakeSize = 300.0,
                intakeCaffeine = 100.0,
                consumedAt = nextDay  // 00:30 섭취
            )
        )

        val result = useCase.execute(records, sleepStartTime = sleepStart)

        // 00:30은 19:00 이후가 아님
        assertEquals(0, result.lateNightCaffeineCount)
        // 01:00 수면 기준 30분 전이므로 취침 직전 카운팅
        assertEquals(1, result.beforeSleepCaffeineCount)
    }

    // 테스트 5: 카페인 기록이 없는 경우
    @Test
    fun `카페인 기록이 없으면 모든 값이 0이다`() {
        val result = useCase.execute(
            records = emptyList(),
            sleepStartTime = toMillis(23, 0)
        )

        assertEquals(0, result.lateNightCaffeineCount)
        assertEquals(0, result.beforeSleepCaffeineCount)
        assertEquals(0.0, result.residualCaffeineAtSleep, 0.0)
    }
}