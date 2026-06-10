package com.snoffee.wear.presentation.home

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlin.math.roundToInt

enum class CaffeineRiskLevel {
    SAFE, CAUTION, DANGER
}

data class WatchHomeUiState(
    val residualCaffeineMg: Double = 0.0,
    val riskLevel: CaffeineRiskLevel = CaffeineRiskLevel.SAFE,
    val metabolismTime: String = "--:--",
    val concentrationLevel: String = "-",
    val isLoading: Boolean = false,
    val isDataEmpty: Boolean = true
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

    val maxCaffeineMg = 300f
    val progress by animateFloatAsState(
        targetValue = (uiState.residualCaffeineMg.toFloat() / maxCaffeineMg).coerceIn(0f, 1f),
        label = "watchCaffeineGaugeProgress"
    )
    val gaugeColor = when (uiState.riskLevel) {
        CaffeineRiskLevel.SAFE -> Color(0xFF5C4B51)
        CaffeineRiskLevel.CAUTION -> Color(0xFFD9A441)
        CaffeineRiskLevel.DANGER -> Color(0xFFD96C6C)
    }

    val riskText = when (uiState.riskLevel) {
        CaffeineRiskLevel.SAFE -> "안전"
        CaffeineRiskLevel.CAUTION -> "주의"
        CaffeineRiskLevel.DANGER -> "위험"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (!isPhoneConnected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.8f))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("⚠️ 단독 모드 실행 중", color = Color.White, fontSize = 11.sp)
            }
        }

        // 원형 프로그레스 인디케이터
        if (!uiState.isLoading) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxSize(),
                indicatorColor = gaugeColor,
                trackColor = Color(0xFFE8E1DC).copy(alpha = 0.3f),
                strokeWidth = 8.dp
            )
        }
        // 예외처리
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(indicatorColor = colors.primary)
            }

            connectionError -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("⚠️ 연결 실패", style = typography.caption1, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onRetryClick) { Text("재시도") }
                }
            }

            else -> {
                // 정상
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "잔류 카페인",
                        style = typography.caption2.copy(color = colors.onSurface)
                    )
                    Text(
                        text = riskText,
                        style = typography.caption2.copy(color = gaugeColor)
                    )
                    Text(
                        text = "${uiState.residualCaffeineMg.roundToInt()} mg",
                        style = typography.title1.copy(color = colors.onSurface)
                    )
                    Text(
                        text = "대사 완료까지",
                        style = typography.caption1.copy(color = colors.onBackground)
                    )
                    Text(
                        text = uiState.metabolismTime,
                        style = typography.caption1.copy(color = colors.onBackground)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onAddCaffeineClick,
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(30.dp),
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
}