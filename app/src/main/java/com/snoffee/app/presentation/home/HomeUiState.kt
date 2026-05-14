package com.snoffee.app.presentation.home

data class HomeUiState(
    val residualCaffeineMg: Int = 0,
    val riskLevel: CaffeineRiskLevel = CaffeineRiskLevel.SAFE,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    //나중에 가져오기
    val metabolismTime: String = "--:--",
    val concentrationLevel: String = "-",
    val recentLogs: List<String> = emptyList()
)

enum class CaffeineRiskLevel {
    SAFE, CAUTION, DANGER
}