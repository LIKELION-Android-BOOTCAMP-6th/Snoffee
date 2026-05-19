package com.snoffee.app.presentation.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
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
import com.snoffee.app.core.ui.theme.BarChartPrimary
import com.snoffee.app.core.ui.theme.BarChartSecondary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted

@Composable
fun WeeklyReportView(uiState: ReportUiState) {
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
                            text = "210",
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
                            text = "6",
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "h ",
                            color = SnoffeeTextMuted,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "45",
                            color = SnoffeeTextMain,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "m",
                            color = SnoffeeTextMuted,
                            fontSize = 14.sp
                        )
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "수면 및 카페인 분석",
                        color = SnoffeeTextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // 우측 상단 더보기 버튼 기획 레이아웃 반영
                    Text(
                        text = "•••",
                        color = SnoffeeTextHint,
                        modifier = Modifier.clickable { }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // [차트 그래픽 공간 더미]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📊 [월·화·수·목·금·토·일] 요일별 이중 바 차트 레이아웃",
                        color = SnoffeeTextHint,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── 차트 범례  ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 수면 점수 컬러 칩
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(BarChartPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "수면 점수", color = SnoffeeTextMuted, fontSize = 12.sp)

                    Spacer(modifier = Modifier.width(24.dp))

                    // 카페인 컬러 칩
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(BarChartSecondary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "카페인 (mg)", color = SnoffeeTextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}