package com.snoffee.app.domain.util

import javax.inject.Inject
import kotlin.math.pow

// 반감기 기반 잔류량 계산, 대사율 보정, 누적 합산
// 공식이 바뀌면 이 파일만 수정하면 됨

class CaffeineCalculator @Inject constructor() {
    fun calculateResidualCaffeine(
        intakeCaffeine: Double,
        consumedAt: Long,
        currentTimeMillis: Long = System.currentTimeMillis(),
        halfLifeHours: Double
    ): Double {
        require(intakeCaffeine >= 0) { "카페인 양은 0 이상이어야 합니다." }
        require(halfLifeHours > 0) { "반감기는 0보다 커야 합니다." }
        require(consumedAt <= currentTimeMillis) { "섭취 시간이 현재 시간보다 미래일 수 없습니다." }
        val elapsedMillis = currentTimeMillis - consumedAt
        val elapsedHours = elapsedMillis / (1000.0 * 60 * 60)
        return intakeCaffeine * (0.5).pow(elapsedHours / halfLifeHours)
    }
}