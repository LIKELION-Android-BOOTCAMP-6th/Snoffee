package com.snoffee.app.domain.util

import javax.inject.Inject
import kotlin.math.log2
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

    //공식 -> 0 수렴 불가 => 01mg 미만 시간으로 계싼
    fun calculateZeroCaffeineTime(
        intakeCaffeine: Double,
        consumedAt: Long,
        halfLifeHours: Double
    ): Long {
        val targetMinCaffeine = 0.1 // 0mg에 수렴하는 기준값

        if (intakeCaffeine <= targetMinCaffeine) return consumedAt

        // 0.1mg까지 떨어지는 데 필요한 시간(Hour) 역산 공식
        val requiredHours = halfLifeHours * log2(intakeCaffeine / targetMinCaffeine)
        val requiredMillis = (requiredHours * 60 * 60 * 1000).toLong()

        return consumedAt + requiredMillis
    }
}