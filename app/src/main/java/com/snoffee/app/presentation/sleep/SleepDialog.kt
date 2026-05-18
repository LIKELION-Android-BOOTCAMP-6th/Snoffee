package com.snoffee.app.presentation.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimaryLight
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceElevated
import com.snoffee.app.core.ui.theme.SnoffeeSurfaceOverlay
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.domain.model.SleepRecord
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepDialog(
    onDismiss: () -> Unit,
    onSave: (SleepRecord) -> Unit
) {
    val zoneId = ZoneId.systemDefault()

    // 상태 관리 (사용자 선택 값)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var bedTime by remember { mutableStateOf(LocalTime.of(23, 0)) }
    var wakeUpTime by remember { mutableStateOf(LocalTime.of(7, 0)) }
    var satisfactionIdx by remember { mutableStateOf(2) } // 0~4 인덱스

    // Picker 노출 여부
    var showDatePicker by remember { mutableStateOf(false) }
    var showBedTimePicker by remember { mutableStateOf(false) }
    var showWakeUpTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 E요일")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // 최종 확인창 노출 여부, 데이터 임시 저장 상태
    var showConfirmDialog by remember { mutableStateOf(false) }
    var tempRecord by remember { mutableStateOf<SleepRecord?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = SnoffeeSurfaceElevated
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 헤더 영역
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 12.dp, y = (-12).dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_close_button),
                            contentDescription = "닫기",
                            tint = SnoffeePrimary
                        )
                    }
                }

                Text(
                    "수면 기록",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )
                Text(
                    "오늘 하루 어떠셨나요?",
                    color = SnoffeeTextMuted,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 날짜 선택 필드
                SleepInputField(
                    label = "날짜",
                    value = selectedDate.format(dateFormatter),
                    iconId = R.drawable.ic_calendar,
                    modifier = Modifier.clickable { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 취침 시간 > 기상시간인지 체크
                val isOvernight = bedTime.isAfter(wakeUpTime)
                val bedDateFormatter = DateTimeFormatter.ofPattern("M/d")

                // 시간 선택 필드 (취침 / 기상)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SleepInputField(
                        label = "취침 시간",
                        value = if (isOvernight) {
                            "${selectedDate.minusDays(1).format(bedDateFormatter)} ${
                                bedTime.format(
                                    timeFormatter
                                )
                            }"
                        } else {
                            bedTime.format(timeFormatter)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showBedTimePicker = true }
                    )
                    SleepInputField(
                        label = "기상 시간",
                        value = wakeUpTime.format(timeFormatter),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showWakeUpTimePicker = true }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 수면 만족도 선택
                Text(
                    "수면 만족도",
                    style = MaterialTheme.typography.labelLarge,
                    color = SnoffeeTextMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                SatisfactionRow(
                    selectedIdx = satisfactionIdx,
                    onSelected = { satisfactionIdx = it }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 저장 버튼
                Button(
                    onClick = {
                        // 취침 시간이 기상 시간보다 늦으면 '전날'로 계산하는 로직
                        val adjustedBedDate = if (bedTime.isAfter(wakeUpTime)) {
                            selectedDate.minusDays(1)
                        } else {
                            selectedDate
                        }

                        val startTimestamp = LocalDateTime.of(adjustedBedDate, bedTime)
                            .atZone(zoneId).toInstant().toEpochMilli()
                        val endTimestamp = LocalDateTime.of(selectedDate, wakeUpTime)
                            .atZone(zoneId).toInstant().toEpochMilli()
                        val dateTimestamp =
                            selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()

                        tempRecord = SleepRecord(
                            date = dateTimestamp,
                            startTime = startTimestamp,
                            endTime = endTimestamp,
                            satisfaction = satisfactionIdx + 1
                        )
                        showConfirmDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnoffeePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("저장하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                //최종 확인 다이얼로그
                if (showConfirmDialog && tempRecord != null) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDialog = false },
                        title = { Text(text = "입력 내용 확인") },
                        text = {
                            Column {
                                Text("날짜: ${selectedDate.format(dateFormatter)}")
                                Text(
                                    "시간: ${bedTime.format(timeFormatter)} ~ ${
                                        wakeUpTime.format(
                                            timeFormatter
                                        )
                                    }"
                                )
                                Text("만족도: ${tempRecord!!.satisfaction}점")
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "위 내용으로 저장하시겠습니까?",
                                    fontWeight = FontWeight.Bold,
                                    color = SnoffeeTextMain
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onSave(tempRecord!!)
                                    showConfirmDialog = false
                                    onDismiss()
                                }
                            ) { Text("확인", color = SnoffeeTextMain) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDialog = false }) {
                                Text("취소", color = SnoffeeTextHint)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        containerColor = SnoffeePrimaryLight
                    )
                }
            }
        }
    }

    // --- Picker ---
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("확인") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showBedTimePicker) {
        SnoffeeTimePicker(
            initialTime = bedTime,
            onTimeSelected = { bedTime = it; showBedTimePicker = false },
            onDismiss = { showBedTimePicker = false })
    }

    if (showWakeUpTimePicker) {
        SnoffeeTimePicker(
            initialTime = wakeUpTime,
            onTimeSelected = { wakeUpTime = it; showWakeUpTimePicker = false },
            onDismiss = { showWakeUpTimePicker = false })
    }
}

@Composable
fun SleepInputField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconId: Int? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = SnoffeeTextHint
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            color = SnoffeeSurfaceOverlay,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    color = SnoffeeTextMain
                )
                if (iconId != null) {
                    Icon(
                        painter = painterResource(id = iconId),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SnoffeePrimaryLight
                    )
                }
            }
        }
    }
}

@Composable
fun SatisfactionRow(selectedIdx: Int, onSelected: (Int) -> Unit) {
    //후에 이모지 아이콘 추가
    val emojis = listOf("😫", "🙁", "😐", "🙂", "😄")
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        emojis.forEachIndexed { index, emoji ->
            val isSelected = index == selectedIdx
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isSelected) Color(0xFF5D4037) else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) { Text(text = emoji, fontSize = 24.sp) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoffeeTimePicker(
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = false
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = timePickerState)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("취소") }
                    TextButton(onClick = {
                        onTimeSelected(
                            LocalTime.of(
                                timePickerState.hour,
                                timePickerState.minute
                            )
                        )
                    }) { Text("확인") }
                }
            }
        }
    }
}