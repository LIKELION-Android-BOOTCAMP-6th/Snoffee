package com.snoffee.app.presentation.home

data class HomeUiState(
    val residualCaffeineMg: Int = 0,
    val riskLevel: CaffeineRiskLevel = CaffeineRiskLevel.SAFE,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class CaffeineRiskLevel {
    SAFE, CAUTION, DANGER
}