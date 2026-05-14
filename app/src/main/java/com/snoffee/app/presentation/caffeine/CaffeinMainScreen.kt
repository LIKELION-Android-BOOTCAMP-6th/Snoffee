package com.snoffee.app.presentation.caffeine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import com.snoffee.app.domain.model.CaffeineRecord
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// todo :: 모든 디자인은 공통 부분이 추가되면 해당 공통 컴포넌트 사용하기

// Data
data class CalendarDay(
    val date: LocalDate?,
    val isCurrentMonth: Boolean = true,
    val isSelected: Boolean = false,
    val hasDot: Boolean = false,        // 섭취 기록 있는 날
)

/**
 * YearMonth를 받아 일요일 시작 6×7 그리드를 만들어 반환.
 * selectedDate: 오늘 또는 선택된 날
 * recordedDates: 섭취 기록이 있는 날짜 집합
 */
fun buildCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    recordedDates: Set<LocalDate> = emptySet(),
): List<CalendarDay> {
    val firstDay = yearMonth.atDay(1)
    // 일요일=0, 월요일=1 ... 토요일=6
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()

    val grid = mutableListOf<CalendarDay>()

    // 이전 달 채우기
    val prevMonth = yearMonth.minusMonths(1)
    val prevDays = prevMonth.lengthOfMonth()
    repeat(startOffset) { i ->
        val d = prevMonth.atDay(prevDays - startOffset + i + 1)
        grid.add(CalendarDay(date = d, isCurrentMonth = false))
    }

    // 이번 달
    for (day in 1..daysInMonth) {
        val d = yearMonth.atDay(day)
        grid.add(
            CalendarDay(
                date = d,
                isCurrentMonth = true,
                isSelected = d == selectedDate,
                hasDot = d in recordedDates,
            )
        )
    }

    // 다음 달 채우기 (6주 = 42칸 고정)
    val nextMonth = yearMonth.plusMonths(1)
    var nextDay = 1
    while (grid.size < 42) {
        val d = nextMonth.atDay(nextDay++)
        grid.add(CalendarDay(date = d, isCurrentMonth = false))
    }

    return grid
}

// Screen
@Composable
fun CaffeineMainScreen(
    viewModel: CaffeineMainViewModel = hiltViewModel(),
    onRecordClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val calendarGrid = remember(uiState.currentYearMonth, uiState.selectedDate) {
        buildCalendarGrid(
            yearMonth = uiState.currentYearMonth,
            selectedDate = uiState.selectedDate,
            recordedDates = uiState.recordedDates,
        )
    }

    val monthLabel = uiState.currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy년 M월"))
    val selectedLabel = uiState.selectedDate.format(DateTimeFormatter.ofPattern("M월 d일"))

    // 에러 스낵바
    val snackBarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackBarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.onErrorDismiss()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
    ) { innerPadding ->
        when {
            // Loading
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            // 정상
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    MonthlyCalendarCard(
                        monthLabel = monthLabel,
                        onPrevMonth = viewModel::onPrevMonth,
                        onNextMonth = viewModel::onNextMonth,
                        grid = calendarGrid,
                        onDayClick = { day ->
                            day.date?.let {
                                viewModel.onDateSelected(it)
                            }
                        },
                    )

                    TodaySummarySection(
                        date = selectedLabel,
                        totalMg = uiState.totalIntakeMg,
                        limitMg = uiState.dailyLimitMg,
                        remainMg = uiState.remainMg,
                        progress = uiState.progress,
                    )

                    ActivityLogSection(
                        records = uiState.todayRecords,
                        isLoading = false,
                    )

                    Button(
                        onClick = onRecordClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.caffeine_record_button),
                            fontWeight = FontWeight.Medium,
                            fontSize = 20.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// 월 선택 헤더
@Composable
private fun MonthSelectorHeader(
    monthLabel: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_left),
                    contentDescription = "이전 달",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onPrevMonth() },
                )
                Text(
                    text = monthLabel,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = "다음 달",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onNextMonth() },
                )
            }
        }
    }
}

// 월별 캘린더 카드
private val DAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
private fun MonthlyCalendarCard(
    monthLabel: String,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    grid: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // 월 선택 헤더 (카드 안으로 이동)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_left),
                        contentDescription = stringResource(R.string.caffeine_prev_month_desc),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = monthLabel,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = stringResource(R.string.caffeine_next_month_desc),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 요일 헤더
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DAY_LABELS.forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // 6주 그리드
                grid.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        week.forEach { day ->
                            CalendarDayCell(
                                day = day,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(day) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    val selectedBg = MaterialTheme.colorScheme.secondaryContainer

    val textColor = when {
        !day.isCurrentMonth -> muted.copy(alpha = 0.3f)
        day.isSelected -> primary
        else -> onSurface
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .size(36.dp)
                .then(
                    if (day.isSelected) Modifier.background(
                        selectedBg,
                        CircleShape
                    ) else Modifier
                )
                .clickable(enabled = day.date != null, onClick = onClick),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = day.date?.dayOfMonth?.toString() ?: "",
                fontSize = 16.sp,
                fontWeight = if (day.isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                textAlign = TextAlign.Center,
            )
            // 섭취 기록 점
            if (day.hasDot && day.isCurrentMonth) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            if (day.isSelected) primary else primary.copy(alpha = 0.5f),
                            CircleShape,
                        )
                )
            }
        }
    }
}

// 하루 섭취량
@Composable
private fun TodaySummarySection(
    date: String,
    totalMg: Double,
    limitMg: Double,
    remainMg: Double,
    progress: Float,
) {
    val extColors = SnoffeeTheme.colors  // 확장 색상 (그라디언트용)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.caffeine_today_summary_label),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.caffeine_today_intake, date),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(R.string.caffeine_remain_amount, totalMg.toInt()),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = stringResource(R.string.caffeine_daily_limit, limitMg.toInt()),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 3.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        extColors.barChartSecondary,        // 연한 갈색 시작
                                        MaterialTheme.colorScheme.primary,  // 진한 갈색 끝
                                    )
                                )
                            ),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_outline_coffee),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.caffeine_remain_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "${remainMg.toInt()}mg",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// 활동 로그
@Composable
private fun ActivityLogSection(
    records: List<CaffeineRecord>,
    isLoading: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.caffeine_activity_log_title),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )

        when {
            // Empty 상태
            records.isEmpty() -> {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_outline_coffee),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = stringResource(R.string.caffeine_empty_log),
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }

            // 리스트
            else -> {
                records.forEachIndexed { index, record ->
                    CaffeineLogItemCard(
                        record = record,
                        isPrimary = index % 2 == 0,  // 홀짝으로 색상 구분
                    )
                }
            }
        }
    }
}

@Composable
private fun CaffeineLogItemCard(
    record: CaffeineRecord,
    isPrimary: Boolean = true,
) {
    val iconBgColor = if (isPrimary) MaterialTheme.colorScheme.secondaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val iconTintColor = if (isPrimary) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.onSurfaceVariant

    // consumedAt(ms) → 시간 문자열 변환
    val timeLabel = remember(record.consumedAt) {
        val ldt = java.time.Instant.ofEpochMilli(record.consumedAt)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
        val hour = ldt.hour
        val minute = ldt.minute
        val amPm = if (hour < 12) "오전" else "오후"
        val h = if (hour % 12 == 0) 12 else hour % 12
        "$amPm ${h}:${minute.toString().padStart(2, '0')}"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 아이콘 영역
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_expand_circle_down_24),
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // 음료명
                    Text(
                        text = record.drinkName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    // 브랜드 · 시간 · 카페인량
                    Text(
                        text = "${record.brandName}  •  $timeLabel  •  ${record.intakeCaffeine.toInt()}mg",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = {
                /* TODO: 삭제 옵션 메뉴 */
            }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.caffeine_more_options_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
