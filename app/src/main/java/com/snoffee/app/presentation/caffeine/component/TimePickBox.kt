package com.snoffee.app.presentation.caffeine.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TimePickerBox(
    selectedTime: LocalTime,
    onTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = remember { DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH) }
    val formatted = selectedTime.format(formatter)

    if (showDialog) {
        TimePickerDialog(
            initialTime = selectedTime,
            onConfirm = { hour, minute ->
                onTimeChange(LocalTime.of(hour, minute))
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    BoxVariant(
        formatted = formatted,
        modifier = modifier,
        onClick = { showDialog = true }
    )
}

// 시간 표시 부분 컴포저블
@Composable
private fun BoxVariant(
    formatted: String,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val colorScheme = SnoffeeTheme.colorScheme
    val extColors = SnoffeeTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.caffeine_drink_time),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp) // 그림자가 그려질 최소 공간 확보
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.2f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(16.dp))    // 클릭했을 때 이펙트 round처리
                    .clickable { onClick() },
                shape = RoundedCornerShape(16.dp),
                color = colorScheme.surfaceVariant.copy(alpha = 0.9f),
                border = BorderStroke(0.5.dp, colorScheme.outlineVariant),
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    Text(
                        text = formatted.uppercase(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.caffeine_drink_time_info),
            fontSize = 14.sp,
            color = extColors.textDisabled,
            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
        )
    }
}

// 시간조절 Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = SnoffeeTheme.colorScheme

    val timePickerState =
        rememberTimePickerState(  // rememberTimePickerState : Material3의 TimePicker에 시간 상태를 연결
            initialHour = initialTime.hour,
            initialMinute = initialTime.minute,
            is24Hour = false
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.caffeine_drink_time_choose),
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = colorScheme.surfaceVariant,
                    clockDialSelectedContentColor = colorScheme.onPrimary,
                    clockDialUnselectedContentColor = colorScheme.onSurface,
                    selectorColor = colorScheme.primary,
                    periodSelectorBorderColor = colorScheme.outline,
                    periodSelectorSelectedContainerColor = colorScheme.primaryContainer,
                    periodSelectorUnselectedContainerColor = colorScheme.surface,
                    periodSelectorSelectedContentColor = colorScheme.onPrimaryContainer,
                    periodSelectorUnselectedContentColor = colorScheme.onSurfaceVariant,
                    timeSelectorSelectedContainerColor = colorScheme.primaryContainer,
                    timeSelectorUnselectedContainerColor = colorScheme.surfaceVariant,
                    timeSelectorSelectedContentColor = colorScheme.onPrimaryContainer,
                    timeSelectorUnselectedContentColor = colorScheme.onSurfaceVariant
                )
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("확인", color = colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = colorScheme.onSurfaceVariant)
            }
        },
        containerColor = colorScheme.surface,
        shape = RoundedCornerShape(28.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewTimePickerBox() {
    SnoffeeTheme {
        Box(Modifier.padding(16.dp)) {
            TimePickerBox(
                selectedTime = LocalTime.of(9, 45),
                onTimeChange = {}
            )
        }
    }
}