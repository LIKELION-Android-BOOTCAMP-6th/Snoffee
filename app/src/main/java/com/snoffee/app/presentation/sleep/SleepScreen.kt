package com.snoffee.app.presentation.sleep

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.BadSleep
import com.snoffee.app.core.ui.theme.GoodSleep
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeDivider
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimaryLight
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.core.util.Utils.toMonthLabel
import com.snoffee.app.domain.model.SleepData
import com.snoffee.app.domain.model.SleepSource
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val DAY_LABELS = listOf("일", "월", "화", "수", "목", "금", "토")

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

    var deleteTargetData by remember { mutableStateOf<SleepData?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showLimitAlert by remember { mutableStateOf(false) }

    val sleepGrid = remember(
        uiState.currentYearMonth,
        uiState.selectedDate,
        uiState.dailyScores
    ) {
        buildSleepCalendarGrid(
            uiState.currentYearMonth,
            uiState.selectedDate,
            uiState.dailyScores
        )
    }

    val context = LocalContext.current

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkHealthPermission()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.isSaveSuccess) {
        if (uiState.isSaveSuccess) {
            showSleepDialog = false
            editTargetData = null
            viewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!uiState.hasHealthPermission) {
            SleepPermissionEmptyView(
                modifier = Modifier
                    .fillMaxSize(),
                onPermissionClick = {
                    val status = HealthConnectClient.getSdkStatus(context)

                    if (status == HealthConnectClient.SDK_AVAILABLE) {
                        context.startActivity(
                            Intent(
                                HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS
                            )
                        )
                    } else {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.google.android.apps.healthdata")
                            )
                        )
                    }
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SnoffeeBgBase)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SleepCalendarCard(
                    monthLabel = uiState.currentYearMonth.toMonthLabel(),
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

                if (uiState.selectedDateRecords.isEmpty()) {
                    // 기록이 없을 때
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = SnoffeeSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "기록된 수면 데이터가 없습니다.",
                            modifier = Modifier.padding(24.dp),
                            textAlign = TextAlign.Center,
                            color = SnoffeeTextMuted,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // 기록이 1개 이상 있을 때 리스트 형태로 표출
                    uiState.selectedDateRecords.forEachIndexed { index, record ->
                        val durationMillis = record.sleepEnd - record.sleepStart
                        val hours = durationMillis / (1000 * 60 * 60)
                        val minutes = (durationMillis / (1000 * 60)) % 60
                        val timeLabel = "${hours}h ${minutes}m"

                        val isManualInput = record.source == SleepSource.MANUAL
                        val sourceLabel = if (isManualInput) "수동 입력" else "삼성 헬스"

                        Column {
                            Text(
                                text = "기록 #${index + 1} ($sourceLabel)",
                                fontSize = 12.sp,
                                color = if (isManualInput) SnoffeePrimary else SnoffeeTextMuted,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                            )

                            // 각각의 기록 정보 카드
                            SleepInfoCard(
                                time = timeLabel,
                                score = record.deepSleepRatio,
                                labelPrefix = "수면"
                            )

                            //수동 입력(MANUAL) 데이터일 때만 수정/삭제 버튼을 화면에 렌더링
                            if (isManualInput) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp, bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    TextButton(
                                        onClick = {
                                            editTargetData = record
                                            showSleepDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .border(1.dp, SnoffeePrimary, RoundedCornerShape(8.dp)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "수정",
                                            color = SnoffeePrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            deleteTargetData = record
                                            showDeleteConfirmDialog = true
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFFFEBEE),
                                            contentColor = Color(0xFFC62828)
                                        )
                                    ) {
                                        Text(
                                            "삭제",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.selectedDateRecords.any { it.source != SleepSource.MANUAL }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    SnoffeePrimarySubtle.copy(alpha = 0.45f),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ⓘ",
                                color = SnoffeePrimary
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "삼성 헬스 수면 점수와 다른 자체 기준 점수 입니다.",
                                color = SnoffeeTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = SnoffeeDivider
                )

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
                        if (uiState.selectedDateRecords.size >= 3) {
                            showLimitAlert = true // 3개 이상이면 추가 금지 팝업
                        } else {
                            editTargetData = null
                            showSleepDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.selectedDateRecords.size >= 3) Color.Gray else SnoffeePrimary
                    )
                ) {
                    Text(
                        text = if (uiState.selectedDateRecords.size >= 3) "수면 추가 완료 (최대 3개)" else "수면 추가하기",
                        fontSize = 20.sp
                    )
                }
            }
        }
    }

    if (showSleepDialog || uiState.isSavingError) {
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
            initialData = editTargetData,
            defaultDate = uiState.selectedDate,
            existingRecords = uiState.selectedDateRecords
        )
    }

    if (showLimitAlert) {
        AlertDialog(
            onDismissRequest = { showLimitAlert = false },
            title = { Text("등록 제한 안내", fontWeight = FontWeight.Bold) },
            text = { Text("수면 기록은 하루에 최대 3개까지만 기입할 수 있습니다.") },
            confirmButton = {
                TextButton(onClick = { showLimitAlert = false }) {
                    Text("확인", color = SnoffeePrimary)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = SnoffeePrimaryLight
        )
    }

    if (showDeleteConfirmDialog && deleteTargetData != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmDialog = false
                deleteTargetData = null
            },
            title = {
                Text(
                    text = "수면 기록 삭제",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "선택하신 수면 기록을 삭제하시겠습니까?\n삭제된 데이터는 복구할 수 없습니다.",
                    fontSize = 15.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSleepRecord(deleteTargetData!!)
                        showDeleteConfirmDialog = false
                        deleteTargetData = null
                    }
                ) {
                    Text(
                        text = "삭제",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        deleteTargetData = null
                    },
                ) {
                    Text(text = "취소", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = SnoffeeSurface
        )
    }
}

@Composable
private fun SleepPermissionEmptyView(
    modifier: Modifier = Modifier,
    onPermissionClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(SnoffeeBgBase)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Bedtime,
            contentDescription = null,
            tint = SnoffeePrimary,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "수면 데이터 권한이 필요해요",
            color = SnoffeeTextMain,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Health Connect 권한을 허용하면\n수면 데이터를 기반으로 분석을 제공해드려요.",
            color = SnoffeeTextMuted,
            fontSize = 14.sp,
            lineHeight = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onPermissionClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SnoffeePrimary
            )
        ) {
            Text(
                text = "권한 설정하러 가기",
                fontWeight = FontWeight.Bold
            )
        }
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

    // 이전 달 채우기
    val prevMonth = yearMonth.minusMonths(1)
    val prevDays = prevMonth.lengthOfMonth()
    repeat(startOffset) { i ->
        val date = prevMonth.atDay(prevDays - startOffset + i + 1)
        grid.add(
            SleepCalendarDay(
                date = date,
                isCurrentMonth = false,
                isSelected = date == selectedDate,
                isToday = date == today,
                score = dailyScores[date]
            )
        )
    }

    // 이번 달
    for (day in 1..daysInMonth) {
        val date = yearMonth.atDay(day)

        grid.add(
            SleepCalendarDay(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == today,
                score = dailyScores[date]
            )
        )
    }

    // 다음 달 채우기 (6주 = 42칸 고정)
    val nextMonth = yearMonth.plusMonths(1)
    var nextDay = 1
    while (grid.size < 42) {
        val date = nextMonth.atDay(nextDay++)
        grid.add(
            SleepCalendarDay(
                date = date,
                isCurrentMonth = false,
                isSelected = date == selectedDate,
                isToday = date == today,
                score = dailyScores[date]
            )
        )
    }

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
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SnoffeeSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPrev) {
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

                IconButton(onClick = onNext) {
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
                            SleepDayCell(
                                day = day,
                                modifier = Modifier.weight(1f),
                                onClick = { onDayClick(day) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LegendItem(GoodSleep, "좋은 수면")

                    Spacer(modifier = Modifier.width(16.dp))

                    LegendItem(BadSleep, "부족 수면")
                }
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
        day.score != null && day.score >= 90 -> GoodSleep
        day.score != null && day.score <= 59 -> BadSleep
        else -> Color.Transparent
    }

    val contentColor = when {
        !day.isCurrentMonth -> SnoffeeTextMain.copy(alpha = 0.3f)
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
                if (day.isSelected) {
                    Modifier.border(2.dp, SnoffeePrimary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                enabled = day.date != null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = day.date?.dayOfMonth?.toString() ?: "",
                fontSize = 16.sp,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(
                        // 기록이 추가되면 dot 영역 때문에 날짜가 들쑥 날쑥이 되어서 코드 수정
                        color = if (day.score != null) SnoffeePrimary else Color.Transparent,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = time,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )

                Text(
                    text = "$labelPrefix 시간",
                    fontSize = 12.sp,
                    color = SnoffeeTextMuted
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(SnoffeeDivider)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (score > 0) {
                        "${score}점"
                    } else {
                        "--"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )

                Text(
                    text = "$labelPrefix 점수",
                    fontSize = 12.sp,
                    color = SnoffeeTextMuted
                )
            }
        }
    }
}