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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.core.ui.theme.SnoffeeInfo
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.ui.theme.SnoffeeWarning
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DailyReportView(uiState: ReportUiState) {
    val displayHours = uiState.todaySleepHours.toString()
    val displayMinutes = String.format(Locale.US, "%02d", uiState.todaySleepMinutes)

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
        // 오늘 요약 대시보드 카드 (2열 가로 배치)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 총 섭취 카페인 카드
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "총 섭취 카페인",
                        color = SnoffeeTextMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = uiState.todayTotalCaffeine.toString(),
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

                // 총 수면 시간 카드
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SnoffeeSurface)
                        .padding(16.dp)
                ) {
                    Text(text = "총 수면 시간", color = SnoffeeTextMuted, fontSize = 12.sp)
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

        // 카페인 기록 타임라인 리스트 카드
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SnoffeeSurface)
                    .padding(16.dp)
            ) {
                Text(
                    text = "오늘의 기록",
                    color = SnoffeeTextMain,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 카페인 및 수면 기록 타임라인 분기
                if (uiState.todayCaffeineRecords.isEmpty() && !uiState.hasTodayRecord) {
                    Text(
                        text = "오늘 등록된 기록이 없습니다.",
                        color = SnoffeeTextHint,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    //카페인 기록 리스트
                    uiState.todayCaffeineRecords.forEach { record ->
                        val formattedTime = runCatching {
                            Instant.ofEpochMilli(record.consumedAt)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
                        }.getOrDefault("시간 정보 없음")

                        // 고카페인(150mg 이상)일 경우 경고 컬러 트리거
                        val isHighCaffeine = record.intakeCaffeine >= 150.0

                        DailyRecordItem(
                            time = formattedTime,
                            title = if (record.brandName.isNotEmpty()) "[${record.brandName}] ${record.drinkName}" else record.drinkName, // ◀ [수정] record.drinkName 반영 및 브랜드명 결합
                            value = "${record.intakeCaffeine.toInt()} mg",
                            typeColor = if (isHighCaffeine) SnoffeeWarning else SnoffeePrimary
                        )
                    }

                    //수면
                    DailyRecordItem(
                        time = uiState.todaySleepStart,
                        title = "수면 기록 시작",
                        value = if (uiState.hasTodayRecord) "수면 진입" else "기록 없음",
                        typeColor = SnoffeeInfo
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyRecordItem(
    time: String,
    title: String,
    value: String,
    typeColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(typeColor, RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))

            // 기록 타이틀 & 시간
            Column {
                Text(
                    text = title,
                    color = SnoffeeTextMain,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = time,
                    color = SnoffeeTextHint,
                    fontSize = 11.sp
                )
            }
        }

        // 수치 데이터 영역
        Text(
            text = value,
            color = SnoffeeTextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}