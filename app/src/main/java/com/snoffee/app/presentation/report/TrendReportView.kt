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
import com.snoffee.app.core.ui.component.InsightCard
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning
import com.snoffee.app.core.util.Utils.toSleepTimeParts
import com.snoffee.app.presentation.report.component.BarChartItem
import com.snoffee.app.presentation.report.component.InteractiveBarChart
import com.snoffee.app.presentation.report.component.SleepValue
import java.time.YearMonth

@Composable
fun TrendReportView(uiState: ReportUiState, onRefreshTrendInsight: () -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 4.dp, 16.dp, 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("전체 기간 수면 평균", color = SnoffeeTextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    SleepValue(
                        parts = uiState.totalAvgSleepTime.toSleepTimeParts(),
                        numberSize = 30.sp
                    )
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
                val currentMonth = YearMonth.now()
                val orderedMonths = listOf(
                    currentMonth.minusMonths(2),
                    currentMonth.minusMonths(1),
                    currentMonth
                ).map { "${it.monthValue}월" }

                val chartDataMap = orderedMonths.associateWith {
                    uiState.monthlyCaffeineTrend[it] ?: 0.0
                }

                val maxEntry = chartDataMap.maxByOrNull { it.value }
                val peakMonth = maxEntry?.takeIf { it.value > 0.0 }?.key ?: "-"
                val peakAmount = maxEntry?.value?.toInt() ?: 0

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "월별 카페인 섭취 추이",
                        color = SnoffeeTextMain,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "최고치: ${peakAmount}mg ($peakMonth)",
                        color = SnoffeeError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                InteractiveBarChart(
                    data = orderedMonths.map { month ->
                        BarChartItem(
                            label = month,
                            value = chartDataMap[month] ?: 0.0
                        )
                    },
                    valueSuffix = "mg",
                    barWidth = 22.dp,
                    barColor = SnoffeeWarning
                )
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
                    text = "월별 수면 점수 분석 결과",
                    color = SnoffeeTextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SnoffeePrimarySubtle)
                            .padding(horizontal = 10.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "BEST",
                            color = SnoffeePrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${uiState.bestMonthLabel} (${uiState.bestMonthScore}점)",
                            color = SnoffeeTextMain,
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SnoffeeBgWarm)
                            .padding(horizontal = 10.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "WORST",
                            color = SnoffeeError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "${uiState.worstMonthLabel} (${uiState.worstMonthScore}점)",
                            color = SnoffeeTextMain,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
        item {
            InsightCard(
                insight = uiState.trendInsight,
                isLoading = uiState.isTrendInsightLoading,
                onRefreshClick = onRefreshTrendInsight,
                emptyText = "분석할 데이터가 충분하지 않아요.",
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}