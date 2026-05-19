package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning

@Composable
fun WeeklyReportView(uiState: ReportUiState) {
    val sleepTimeParts = uiState.weeklyAvgSleepTime.split(" ")
    val weeklyHours = sleepTimeParts.getOrNull(0)?.replace("h", "") ?: "0"
    val weeklyMinutes = sleepTimeParts.getOrNull(1)?.replace("m", "") ?: "00"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 4.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //7일 기준 평균 데이터 요약 (2열 가로 카드 레이아웃)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 주간 평균 카페인 카드
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "주간 평균 카페인",
                        color = SnoffeeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.weeklyAvgCaffeine.toString(),
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " mg",
                            color = SnoffeeTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                // 주간 수면 평균 카드
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "주간 수면 평균",
                        color = SnoffeeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = weeklyHours, color = SnoffeeTextMain,
                            fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                        Text(text = "h ", color = SnoffeeTextMuted, fontSize = 14.sp)
                        Text(
                            text = weeklyMinutes, color = SnoffeeTextMain,
                            fontSize = 24.sp, fontWeight = FontWeight.Bold
                        )
                        Text(text = "m", color = SnoffeeTextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        // 수면 및 카페인 분석 이중 바 차트 영역
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "수면 및 카페인 분석",
                    color = SnoffeeTextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                val days = listOf("월", "화", "수", "목", "금", "토", "일")
                val maxCaffeine =
                    (uiState.weeklyCaffeineChartData.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                val maxSleep =
                    (uiState.weeklySleepChartData.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    days.forEach { day ->
                        val caffeineAmount = uiState.weeklyCaffeineChartData[day] ?: 0.0
                        val sleepAmount = uiState.weeklySleepChartData[day] ?: 0.0

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // 카페인 막대
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .fillMaxHeight(
                                                (caffeineAmount / maxCaffeine).toFloat()
                                                    .coerceIn(0f, 1f)
                                            )
                                            .background(
                                                SnoffeeWarning,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                    // 수면 시간 막대
                                    Box(
                                        modifier = Modifier
                                            .width(8.dp)
                                            .fillMaxHeight(
                                                (sleepAmount / maxSleep).toFloat().coerceIn(0f, 1f)
                                            )
                                            .background(
                                                SnoffeePrimaryDark,
                                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // X축 요일 표시
                            Text(
                                text = day,
                                color = SnoffeeTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}