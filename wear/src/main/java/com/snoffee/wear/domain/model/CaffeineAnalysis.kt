package com.snoffee.wear.domain.model

data class CaffeineAnalysis(
    val residualAmount: Double,
    val cutoffTime: Long,
    val sleepImpactScore: Double
)