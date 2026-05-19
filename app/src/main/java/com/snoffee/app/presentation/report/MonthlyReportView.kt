package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeSuccess
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning

@Composable
fun MonthlyReportView(uiState: ReportUiState) {
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
        //한 달 기준 평균 대시보드 카드
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 월간 평균 카페인
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "월간 평균 카페인",
                        color = SnoffeeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.monthlyAvgCaffeine.toString(),
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

                // 월간 수면 평균
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(text = "월간 수면 평균", color = SnoffeeTextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = displayHours,
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "h ", color = SnoffeeTextMuted, fontSize = 14.sp)
                        Text(
                            text = displayMinutes,
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "m", color = SnoffeeTextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        // 카페인 과다 vs 최소 섭취일 수면 대조 섹션
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

                // 대조 행 — 많이 먹은 날
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "카페인 섭취량이 많았던 날",
                        color = SnoffeeWarning,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "평균 수면 ${uiState.highCaffeineDaySleepTime}",
                        color = SnoffeeTextMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // 대조 행 — 적게 먹은 날 (테마의 SnoffeeSuccess 컬러 사용)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "카페인 섭취량이 적었던 날",
                        color = SnoffeeSuccess,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "평균 수면 ${uiState.lowCaffeineDaySleepTime}",
                        color = SnoffeeTextMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 대조 차트
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(SnoffeeBgBase, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "수면 패턴 변화 대조 그래프 영역",
                        color = SnoffeeTextHint,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}