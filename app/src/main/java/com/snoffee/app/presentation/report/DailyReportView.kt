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
import com.snoffee.app.core.util.SleepTimeParts
import com.snoffee.app.presentation.report.component.SleepValue
import com.snoffee.app.presentation.report.component.StatCard
import com.snoffee.app.presentation.report.component.StatValue
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun DailyReportView(uiState: ReportUiState) {
    val displayHours = uiState.todaySleepHours.toString()
    val displayMinutes = String.format(Locale.KOREAN, "%02d", uiState.todaySleepMinutes)

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
                StatCard(modifier = Modifier.weight(1f), label = "총 섭취 카페인") {
                    StatValue(uiState.todayTotalCaffeine.toString(), " mg")
                }

                // 총 수면 시간 카드
                StatCard(modifier = Modifier.weight(1f), label = "총 수면 시간") {
                    SleepValue(SleepTimeParts(uiState.todaySleepHours, uiState.todaySleepMinutes))
                }
            }
        }

        // 오늘의 기록 타임라인 리스트 카드
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

                // 카페인 기록과 수면 기록이 모두 존재하지 않는 완전 공백 상태 검증
                if (uiState.todayCaffeineRecords.isEmpty() && uiState.todaySleepRecords.isEmpty()) {
                    Text(
                        text = "오늘 등록된 기록이 없습니다.",
                        color = SnoffeeTextHint,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    // 1. 카페인 기록 리스트 루프 출력
                    uiState.todayCaffeineRecords.forEach { record ->
                        val formattedTime = runCatching {
                            Instant.ofEpochMilli(record.consumedAt)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN))
                        }.getOrDefault("시간 정보 없음")

                        val isHighCaffeine = record.intakeCaffeine >= 150.0

                        DailyRecordItem(
                            time = formattedTime,
                            title = if (record.brandName.isNotEmpty()) "[${record.brandName}] ${record.drinkName}" else record.drinkName,
                            value = "${record.intakeCaffeine.roundToInt()} mg",
                            typeColor = if (isHighCaffeine) SnoffeeWarning else SnoffeePrimary
                        )
                    }

                    // 2. ★ [변경] 오늘 등록한 수면 기록이 여러 개(최대 3개)이면 모두 개별 리스트 아이템으로 출력
                    if (uiState.todaySleepRecords.isNotEmpty()) {
                        // 수면 시작 시간 기준으로 깔끔하게 정렬하여 타임라인 정립
                        val sortedSleepRecords =
                            uiState.todaySleepRecords.sortedBy { it.sleepStart }

                        sortedSleepRecords.forEachIndexed { index, sleepData ->
                            val startTime = Instant.ofEpochMilli(sleepData.sleepStart)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm"))

                            val endTime = Instant.ofEpochMilli(sleepData.sleepEnd)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH:mm"))

                            val durationMillis = sleepData.sleepEnd - sleepData.sleepStart
                            val duration = Duration.ofMillis(durationMillis)
                            val hourLabel = duration.toHours()
                            val minuteLabel = duration.toMinutes() % 60

                            // 여러 개인 경우 수면 기록 #1, #2 형태로 동적 네이밍 매핑
                            DailyRecordItem(
                                time = "$startTime ~ $endTime",
                                title = if (sortedSleepRecords.size > 1) "수면 기록 #${index + 1}" else "오늘의 수면",
                                value = "${hourLabel}시간 ${minuteLabel}분 수면",
                                typeColor = SnoffeeInfo
                            )
                        }
                    } else {
                        // 오늘 범위 내에 수면 기록이 하나도 없을 경우의 폴백 홀더
                        DailyRecordItem(
                            time = "--:--",
                            title = "수면 기록",
                            value = "기록 없음",
                            typeColor = SnoffeeInfo
                        )
                    }
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