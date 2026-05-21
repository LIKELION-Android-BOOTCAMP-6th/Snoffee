package com.snoffee.app.presentation.home

import com.snoffee.app.domain.model.CaffeineRecord

data class HomeUiState(
    val residualCaffeineMg: Double = 0.0,
    val riskLevel: CaffeineRiskLevel = CaffeineRiskLevel.SAFE,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val metabolismTime: String = "--:--",
    val concentrationLevel: String = "-",
    val recentLogs: List<CaffeineRecord> = emptyList()
)

enum class CaffeineRiskLevel {
    SAFE, CAUTION, DANGER
}