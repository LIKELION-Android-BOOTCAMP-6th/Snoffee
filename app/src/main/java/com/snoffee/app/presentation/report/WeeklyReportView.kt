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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.component.InsightCard
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.util.Utils.toSleepTimeParts
import com.snoffee.app.presentation.report.component.DoubleBarChartItem
import com.snoffee.app.presentation.report.component.InteractiveDoubleBarChart
import com.snoffee.app.presentation.report.component.SleepValue
import com.snoffee.app.presentation.report.component.StatCard
import com.snoffee.app.presentation.report.component.StatValue

@Composable
fun WeeklyReportView(uiState: ReportUiState, onRefreshWeeklyInsight: () -> Unit) {
    val sleepTimeParts = uiState.weeklyAvgSleepTime.split(" ")
    val weeklyHours = sleepTimeParts.getOrNull(0)?.replace("h", "") ?: "0"
    val weeklyMinutes = sleepTimeParts.getOrNull(1)?.replace("m", "") ?: "00"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), label = "주간 평균 카페인") {
                    StatValue(uiState.weeklyAvgCaffeine.toString(), " mg")
                }

                StatCard(modifier = Modifier.weight(1f), label = "주간 수면 평균") {
                    SleepValue(uiState.weeklyAvgSleepTime.toSleepTimeParts())
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
                    text = "수면 및 카페인 분석",
                    color = SnoffeeTextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                val days = listOf("월", "화", "수", "목", "금", "토", "일")

                InteractiveDoubleBarChart(
                    data = days.map { day ->
                        DoubleBarChartItem(
                            label = day,
                            caffeineValue = uiState.weeklyCaffeineChartData[day] ?: 0.0,
                            sleepValue = uiState.weeklySleepChartData[day] ?: 0.0
                        )
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        item {
            InsightCard(
                insight = uiState.weeklyInsight,
                isLoading = uiState.isWeeklyInsightLoading,
                onRefreshClick = onRefreshWeeklyInsight,
                emptyText = "분석할 데이터가 충분하지 않아요.",
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}