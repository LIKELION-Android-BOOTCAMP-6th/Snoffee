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
import com.snoffee.app.core.ui.theme.SnoffeeBgWarm
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning

@Composable
fun TrendReportView(uiState: ReportUiState) {
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
        // 전체 기간 통합 수면 평균 카드
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "전체 기간 수면 평균", color = SnoffeeTextMuted, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = uiState.totalAvgSleepTime,
                    color = SnoffeeTextMain,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 월별 카페인 섭취 추이
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(16.dp)
            ) {
                // 🛠️ 변수명을 uiState.monthlyCaffeineTrend 로 정확하게 매핑했습니다.
                val chartDataMap: Map<String, Double> = uiState.monthlyCaffeineTrend

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

                    // 최고 실측치 동적 파싱 연동 (람다 식 매개변수 명시로 타입 추론 보장)
                    val maxEntry = chartDataMap.maxByOrNull { entry -> entry.value }
                    val peakMonth = maxEntry?.key ?: "-"
                    val peakAmount = maxEntry?.value?.toInt() ?: 0

                    Text(
                        text = "최고치: ${peakAmount}mg ($peakMonth)",
                        color = SnoffeeError,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // X축 달 순서 정렬 및 최대 한도 구하기 (명확한 타입 제공으로 연산자 에러 방지)
                val orderedMonths: List<String> = chartDataMap.keys.sortedBy { monthName ->
                    monthName.replace("월", "").toIntOrNull() ?: 0
                }
                val highestCaffeineLimit: Double =
                    (chartDataMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    orderedMonths.forEach { month ->
                        val caffeineValue: Double = chartDataMap[month] ?: 0.0
                        val barRatio: Float =
                            (caffeineValue / highestCaffeineLimit).toFloat().coerceIn(0.05f, 1f)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(54.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(22.dp)
                                        .fillMaxHeight(barRatio)
                                        .background(
                                            SnoffeeWarning,
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = month,
                                color = SnoffeeTextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 수면 점수 변화 및 Best & Worst 월 매핑 결과
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
                    // BEST 월 매핑
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
                            text = "BEST",
                            color = SnoffeePrimaryDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.bestMonthLabel} (${uiState.bestMonthScore}점)",
                            color = SnoffeeTextMain,
                            fontSize = 13.sp
                        )
                    }

                    // WORST 월 매핑
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
                            text = "WORST",
                            color = SnoffeeError,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.worstMonthLabel} (${uiState.worstMonthScore}점)",
                            color = SnoffeeTextMain,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}