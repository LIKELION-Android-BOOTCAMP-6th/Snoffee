package com.snoffee.app.presentation.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.core.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 캘린더 날짜 데이터 구조
data class SleepCalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean = true,
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val hasDot: Boolean = false,        // 수면 기록 있는 날
)

@Composable
fun SleepScreen(viewModel: SleepViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //수면 화면 전용 캘린더 그리드 로직
    val sleepGrid = remember(uiState.currentYearMonth, uiState.selectedDate) {
        buildSleepCalendarGrid(uiState.currentYearMonth, uiState.selectedDate)
    }

    Scaffold(containerColor = SnoffeeBgBase) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            //수면 전용 캘린더 카드
            SleepCalendarCard(
                monthLabel = uiState.currentYearMonth.format(DateTimeFormatter.ofPattern("M월 yyyy")),
                onPrev = viewModel::onPrevMonth,
                onNext = viewModel::onNextMonth,
                grid = sleepGrid,
                onDayClick = { day ->
                    day.date?.let { viewModel.onDateSelected(it) }
                }
            )

            //수면 요약
            SleepSummarySection(uiState.averageSleepTime, uiState.averageScore)

            // 수면 추가 버튼
            Button(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(SnoffeePrimary)
            ) {
                Text("수면 추가하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 수면 전용 캘린더 생성 로직
private fun buildSleepCalendarGrid(yearMonth: YearMonth, selectedDate: LocalDate): List<SleepCalendarDay> {
    val today = LocalDate.now()
    val firstDay = yearMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val grid = mutableListOf<SleepCalendarDay>()

    repeat(startOffset) { grid.add(SleepCalendarDay(date = null)) }
    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)
        grid.add(SleepCalendarDay(
            date = date,
            // 선택된 날짜와 오늘 날짜 확인
            isSelected = date == selectedDate,
            isToday = date == today
        ))    }
    while (grid.size < 42) { grid.add(SleepCalendarDay(date = null)) }
    return grid
}

@Composable
private fun SleepCalendarCard(
    monthLabel: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    grid: List<SleepCalendarDay>,
    onDayClick: (SleepCalendarDay) -> Unit
) {
    Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp, color = SnoffeeSurface) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onPrev) { Text("<") }
                Text(monthLabel, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = onNext) { Text(">") }
            }

            // 요일
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray)
                }
            }

            // 날짜 그리드
            grid.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    week.forEach { day ->
                        SleepDayCell(day, Modifier.weight(1f), onClick = { onDayClick(day) })
                    }
                }
            }

            // 범례
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LegendItem(GoodSleep, "좋은 수면")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(BadSleep, "부족 수면")
            }
        }
    }
}

@Composable
private fun SleepDayCell(day: SleepCalendarDay, modifier: Modifier,
                         onClick: () -> Unit) {
    val bgColor = when {
        day.isSelected -> SnoffeePrimary
        day.isToday -> SnoffeePrimarySubtle
        else -> Color.Transparent
    }
    Box(
        modifier = modifier
            .size(36.dp)
            .background(bgColor, CircleShape)
            .clickable(enabled = day.date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date?.dayOfMonth?.toString() ?: "",
            fontSize = 14.sp,
            color = if (day.isSelected) Color.White else SnoffeeTextMain
        )
    }
}

@Composable
private fun SleepSummarySection(time: String, score: Int) {
    Surface(shape = RoundedCornerShape(24.dp), shadowElevation = 2.dp, color = SnoffeeSurface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(time, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SnoffeeTextMain)
                Text("평균 수면 시간", fontSize = 13.sp, color = SnoffeeTextMuted)
            }

            // 중앙 구분선
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(SnoffeeDivider)
            )
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${score}점", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SnoffeeTextMain)
                Text("평균 수면 점수", fontSize =13.sp, color = SnoffeeTextMuted)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}