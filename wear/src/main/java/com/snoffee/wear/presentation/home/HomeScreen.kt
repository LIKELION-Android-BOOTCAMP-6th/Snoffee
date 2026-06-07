package com.snoffee.wear.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

enum class CaffeineRiskLevel {
    SAFE, CAUTION, DANGER
}

data class WatchHomeUiState(
    val residualCaffeineMg: Double = 0.0,
    val riskLevel: CaffeineRiskLevel = CaffeineRiskLevel.SAFE,
    val metabolismTime: String = "--:--",
    val concentrationLevel: String = "-",
    val isLoading: Boolean = false
)

@Composable
fun HomeScreen(
    uiState: WatchHomeUiState,
    isPhoneConnected: Boolean,
    connectionError: Boolean,
    onAddCaffeineClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colors
    val typography = MaterialTheme.typography

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(indicatorColor = colors.primary)
        } else if (connectionError || !isPhoneConnected) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "⚠️ 폰 연결 확인 필요",
                    style = typography.caption1.copy(color = colors.onBackground),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onRetryClick) {
                    Text(text = "재시도", color = colors.onPrimary)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "잔류 카페인",
                    style = MaterialTheme.typography.caption2.copy(color = colors.onSurface)
                )
                Text(
                    text = "${uiState.residualCaffeineMg.toInt()} mg",
                    style = typography.title1.copy(
                        color = when (uiState.riskLevel) {
                            CaffeineRiskLevel.CAUTION, CaffeineRiskLevel.DANGER -> colors.error
                            else -> colors.primary
                        }
                    )
                )
                Text(
                    text = "대사 완료까지: ${uiState.metabolismTime}",
                    style = MaterialTheme.typography.caption1.copy(color = colors.onBackground)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onAddCaffeineClick,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(35.dp),
                    colors = ButtonDefaults.primaryButtonColors(backgroundColor = colors.primary)
                ) {
                    Text(
                        text = "+ 카페인 추가",
                        style = typography.button.copy(color = colors.onPrimary)
                    )
                }
            }
        }
    }
}