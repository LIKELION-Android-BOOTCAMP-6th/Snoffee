package com.snoffee.app.presentation.caffeine.input.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeTheme
import com.snoffee.app.core.util.Utils.toTodayEpochMilli
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.presentation.caffeine.component.TimePickerBox
import java.time.LocalTime

@Composable
fun CaffeineInputDialog(
    onDismiss: () -> Unit = {},
    onConfirm: (CaffeineRecord) -> Unit = {},
) {
    var caffeineAmount by remember { mutableFloatStateOf(0f) }
    var caffeineInputString by remember { mutableStateOf("") }
    var drinkVolume by remember { mutableIntStateOf(0) }
    var volumeInputString by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf<LocalTime>(LocalTime.now()) }
    var drinkName by remember { mutableStateOf("") }
    val caffeineFocusRequester = remember { FocusRequester() }  // 포커스 제어
    val volumeFocusRequester = remember { FocusRequester() }

    val colorScheme = SnoffeeTheme.colorScheme
    val extColors = SnoffeeTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = extColors.surfaceElevated,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 상단 헤더 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.caffeine_drink_input_dialog_title),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface
                    ),
                    modifier = Modifier.padding(start = 4.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_cancel),
                    contentDescription = "닫기",
                    tint = extColors.textDisabled,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.caffeine_drink_input_dialog_drink_name_subtitle),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = SnoffeeTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                    border = BorderStroke(0.5.dp, SnoffeeTheme.colorScheme.outlineVariant)
                ) {
                    Box(
                        contentAlignment = Alignment.CenterStart,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        if (drinkName.isEmpty()) {
                            Text(
                                text = stringResource(R.string.caffeine_drink_input_dialog_drink_name_hint),
                                color = SnoffeeTheme.colors.textHint,
                                fontSize = 16.sp
                            )
                        }

                        // 음료명 실제 입력 필드
                        BasicTextField(
                            value = drinkName,
                            onValueChange = { drinkName = it },
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next,
                                keyboardType = KeyboardType.Text
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    caffeineFocusRequester.requestFocus()
                                }
                            ),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                color = SnoffeeTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 카페인 함량 + 음료 용량 입력 영역
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.caffeine_drink_input_dialog_drink_caffeine_amount_subtitle),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = SnoffeeTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        border = BorderStroke(0.5.dp, SnoffeeTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (caffeineInputString.isEmpty()) {
                                    Text(
                                        text = "0.0",
                                        color = SnoffeeTheme.colors.textHint,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                BasicTextField(
                                    value = caffeineInputString,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() || it == '.' } && newValue.length <= 5) {
                                            caffeineInputString = newValue
                                            newValue.toFloatOrNull()?.let { caffeineAmount = it }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SnoffeeTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(caffeineFocusRequester),
                                    singleLine = true
                                )
                            }

                            Text(
                                text = "mg",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = SnoffeeTheme.colors.textHint,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 음료 용량 입력
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.caffeine_drink_input_dialog_drink_amount_subtitle),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                    )

                    Surface(
                        modifier = Modifier.height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = SnoffeeTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                        border = BorderStroke(0.5.dp, SnoffeeTheme.colorScheme.outlineVariant),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (volumeInputString.isEmpty()) {
                                    Text(
                                        text = "0",
                                        color = SnoffeeTheme.colors.textHint,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                BasicTextField(
                                    value = volumeInputString,
                                    onValueChange = { newValue ->
                                        if (newValue.all { it.isDigit() } && newValue.length <= 5) {
                                            volumeInputString = newValue
                                            newValue.toIntOrNull()?.let { drinkVolume = it }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = SnoffeeTheme.colorScheme.primary,
                                        letterSpacing = 0.5.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .focusRequester(volumeFocusRequester),
                                    singleLine = true
                                )
                            }

                            Text(
                                text = "ml",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = SnoffeeTheme.colors.textHint,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 섭취 시간 영역 (기존 위젯 사용)
            TimePickerBox(
                selectedTime = selectedTime,
                onTimeChange = { selectedTime = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 기록하기 버튼
            Button(
                onClick = {
                    onConfirm(
                        CaffeineRecord(
                            drinkId = "DIRECT_${System.currentTimeMillis()}",
                            drinkName = drinkName,
                            brandName = "직접 입력",
                            intakeSize = drinkVolume.toDouble(),
                            intakeCaffeine = caffeineAmount.toDouble(),
                            consumedAt = selectedTime.toTodayEpochMilli()
                        )
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.caffeine_drink_input_dialog_drink_save),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CaffeineInputDialogPreview() {
    SnoffeeTheme {
        Surface(color = SnoffeeTheme.colorScheme.background) {
            CaffeineInputDialog()
        }
    }
}