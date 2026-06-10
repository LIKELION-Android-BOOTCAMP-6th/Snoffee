package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.snoffee.app.core.ui.theme.SnoffeeSuccess
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning
import com.snoffee.app.presentation.report.component.BarChartItem
import com.snoffee.app.presentation.report.component.InteractiveBarChart

@Composable
fun MonthlyReportView(uiState: ReportUiState, onRefreshMonthlyInsight: () -> Unit) {
    val timeParts = uiState.monthlyAvgSleepTime.split(" ")
    val displayHours = timeParts.getOrNull(0)?.replace("h", "") ?: "0"
    val displayMinutes = timeParts.getOrNull(1)?.replace("m", "") ?: "00"

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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text("월간 평균 카페인", color = SnoffeeTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.monthlyAvgCaffeine.toString(),
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(" mg", color = SnoffeeTextMuted, fontSize = 14.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text("월간 수면 평균", color = SnoffeeTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            displayHours,
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("h ", color = SnoffeeTextMuted, fontSize = 14.sp)
                        Text(
                            displayMinutes,
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("m", color = SnoffeeTextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "카페인 과다 vs 최소 섭취일 수면 대조",
                    color = SnoffeeTextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "카페인 섭취량이 많았던 날",
                        color = SnoffeeWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "평균 수면 ${uiState.highCaffeineDaySleepTime}",
                        color = SnoffeeTextMain,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "카페인 섭취량이 적었던 날",
                        color = SnoffeeSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "평균 수면 ${uiState.lowCaffeineDaySleepTime}",
                        color = SnoffeeTextMain,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                InteractiveBarChart(
                    data = listOf(
                        BarChartItem(
                            label = "많은 날",
                            value = parseSleepHourValue(uiState.highCaffeineDaySleepTime)
                        ),
                        BarChartItem(
                            label = "적은 날",
                            value = parseSleepHourValue(uiState.lowCaffeineDaySleepTime)
                        )
                    ),
                    valueSuffix = "h",
                    barWidth = 36.dp,
                    barColor = SnoffeeWarning
                )
            }
        }
        item {
            ReportInsightCard(
                title = "Snoffee AI 헬스 코치",
                insight = uiState.monthlyInsight,
                isLoading = uiState.isMonthlyInsightLoading,
                onRefreshClick = onRefreshMonthlyInsight
            )
        }
    }
}

private fun parseSleepHourValue(sleepTime: String): Double {
    val parts = sleepTime.split(" ")
    val hours = parts.getOrNull(0)?.replace("h", "")?.toDoubleOrNull() ?: 0.0
    val minutes = parts.getOrNull(1)?.replace("m", "")?.toDoubleOrNull() ?: 0.0
    return hours + minutes / 60.0
}