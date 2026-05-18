package com.snoffee.app.presentation.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.core.ui.theme.BadSleep
import com.snoffee.app.core.ui.theme.GoodSleep
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeDivider
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimaryLight
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.domain.model.SleepData
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// 캘린더 날짜 데이터 구조
data class SleepCalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean = true,
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val hasDot: Boolean = false,
    val score: Int? = null
)

@Composable
fun SleepScreen(viewModel: SleepViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    var editTargetData by remember { mutableStateOf<SleepData?>(null) }

    var showSleepDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val selectedScore = uiState.dailyScores[uiState.selectedDate] ?: 0
    val selectedTime = uiState.dailySleepTimes[uiState.selectedDate] ?: "--"

    val sleepGrid = remember(uiState.currentYearMonth, uiState.selectedDate, uiState.dailyScores) {
        buildSleepCalendarGrid(
            uiState.currentYearMonth,
            uiState.selectedDate,
            uiState.dailyScores
        )
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            showSleepDialog = false
            editTargetData = null
            viewModel.resetState()
        }
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
            SleepCalendarCard(
                monthLabel = uiState.currentYearMonth.format(DateTimeFormatter.ofPattern("M월 yyyy")),
                onPrev = viewModel::onPrevMonth,
                onNext = viewModel::onNextMonth,
                grid = sleepGrid,
                onDayClick = { day ->
                    day.date?.let { viewModel.onDateSelected(it) }
                }
            )

            Text(
                text = "${uiState.selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))} 수면 리포트",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeeTextMain
            )

            // 순수 수면 정보 카드
            SleepInfoCard(
                time = selectedTime,
                score = selectedScore,
                labelPrefix = "기록된"
            )

            // 날짜에 기록이 있을 때만 하단에 수정 / 삭제 버튼 배치
            val hasRecord = selectedTime != "--" && selectedScore != 0
            if (hasRecord) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // [기록 수정] 버튼
                    TextButton(
                        onClick = {
                            val originalData = viewModel.getOriginalSleepData(uiState.selectedDate)

                            if (originalData != null) {
                                editTargetData = originalData
                                showSleepDialog = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .border(1.dp, SnoffeePrimary, RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "기록 수정",
                            color = SnoffeePrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    // [기록 삭제] 버튼 (경고 의미로 소프트 레드 스타일 부여)
                    Button(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFEBEE), // 소프트 핑크/레드
                            contentColor = Color(0xFFC62828)     // 짙은 레드 텍스트
                        )
                    ) {
                        Text("기록 삭제", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SnoffeeDivider)

            Text(
                text = "${uiState.currentYearMonth.monthValue}월 전체 평균",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SnoffeeTextMuted
            )
            SleepInfoCard(
                time = uiState.averageSleepTime,
                score = uiState.averageScore,
                labelPrefix = "평균"
            )

            Button(
                onClick = {
                    editTargetData = null
                    showSleepDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(SnoffeePrimary)
            ) {
                Text("수면 추가하기", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
    if (showSleepDialog) {
        SleepDialog(
            onDismiss = {
                showSleepDialog = false
                editTargetData = null
                viewModel.resetErrorState()
            },
            onSave = { record ->
                viewModel.saveSleepRecord(record)
            },
            isSavingError = uiState.isSavingError,
            onRetry = {
                viewModel.retrySave()
            },
            initialData = editTargetData
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "수면 기록 삭제",
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )
            },
            text = {
                Text(
                    text = "${uiState.selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))}의 수면 기록을 삭제하시겠습니까?\n삭제된 데이터는 복구할 수 없습니다.",
                    color = SnoffeeTextMain
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSleepRecord(uiState.selectedDate)
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text("삭제", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmDialog = false }
                ) {
                    Text("취소", color = SnoffeeTextHint)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = SnoffeePrimaryLight
        )
    }
}

private fun buildSleepCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    dailyScores: Map<LocalDate, Int>
): List<SleepCalendarDay> {
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
            isSelected = date == selectedDate,
            isToday = date == today,
            score = dailyScores[date]
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

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                    Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, color = Color.Gray)
                }
            }

            grid.chunked(7).forEach { week ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)) {
                    week.forEach { day ->
                        SleepDayCell(day, Modifier.weight(1f), onClick = { onDayClick(day) })
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                LegendItem(GoodSleep, "좋은 수면")
                Spacer(modifier = Modifier.width(16.dp))
                LegendItem(BadSleep, "부족 수면")
            }
        }
    }
}

@Composable
private fun SleepDayCell(
    day: SleepCalendarDay,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val statusColor = when {
        day.score != null && day.score >= 85 -> GoodSleep
        day.score != null && day.score <= 59 -> BadSleep
        else -> Color.Transparent
    }

    val contentColor = when {
        statusColor != Color.Transparent -> Color.White
        day.isSelected -> SnoffeePrimary
        else -> SnoffeeTextMain
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(statusColor)
            .then(
                if (day.isSelected) Modifier.border(2.dp, SnoffeePrimary, CircleShape)
                else Modifier
            )
            .clickable(enabled = day.date != null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date?.dayOfMonth?.toString() ?: "",
                fontSize = 14.sp,
                color = contentColor
            )

            if (day.score != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(
                            if (day.isSelected) Color.White else SnoffeePrimary,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}

// ✅ 경고(Warning)의 원인이었던 미사용 변수들을 싹 정리한 순수 카드 컴포넌트
@Composable
private fun SleepInfoCard(
    time: String,
    score: Int,
    labelPrefix: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SnoffeeSurface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(time, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SnoffeeTextMain)
                Text("$labelPrefix 시간", fontSize = 12.sp, color = SnoffeeTextMuted)
            }
            Box(modifier = Modifier
                .width(1.dp)
                .height(40.dp)
                .background(SnoffeeDivider))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (score > 0) "${score}점" else "--",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )
                Text("$labelPrefix 점수", fontSize = 12.sp, color = SnoffeeTextMuted)
            }
        }
    }
}