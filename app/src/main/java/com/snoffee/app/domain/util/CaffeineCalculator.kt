package com.snoffee.app.domain.util

import kotlin.math.pow

object CaffeineCalculator {
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