package com.snoffee.app.presentation.setting

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snoffee.app.R
import com.snoffee.app.core.ui.theme.SnoffeeBgBase
import com.snoffee.app.core.ui.theme.SnoffeeBgMuted
import com.snoffee.app.core.ui.theme.SnoffeeError
import com.snoffee.app.core.ui.theme.SnoffeePrimary
import com.snoffee.app.core.ui.theme.SnoffeePrimaryDark
import com.snoffee.app.core.ui.theme.SnoffeePrimarySubtle
import com.snoffee.app.core.ui.theme.SnoffeeSurface
import com.snoffee.app.core.ui.theme.SnoffeeTextDisabled
import com.snoffee.app.core.ui.theme.SnoffeeTextHint
import com.snoffee.app.core.ui.theme.SnoffeeTextMain
import com.snoffee.app.core.ui.theme.SnoffeeTextMuted
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.presentation.onboarding.component.TimeSettingCard
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onNotificationSettingClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var isDarkMode by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showHeightDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }

    val displayHeight = userProfile?.height?.let {
        if (it % 1.0 == 0.0) "${it.toInt()}cm" else String.format(Locale.US, "%.1fcm", it)
    } ?: "-cm"

    val displayWeight = userProfile?.weight?.let {
        if (it % 1.0 == 0.0) "${it.toInt()}kg" else String.format(Locale.US, "%.1fkg", it)
    } ?: "-kg"
    val rawSleep = userProfile?.userSleepTime?.toString()?.padStart(4, '0') ?: "2230"
    val displaySleepTime = "${rawSleep.substring(0, 2)}:${rawSleep.substring(2, 4)}"

    val rawWake = userProfile?.wakeTime?.toString()?.padStart(4, '0') ?: "0630"
    val displayWakeTime = "${rawWake.substring(0, 2)}:${rawWake.substring(2, 4)}"

    val sensitivityText = when (userProfile?.sensitivity) {
        CaffeineSensitivity.LOW -> "낮음"
        CaffeineSensitivity.SENSITIVE -> "높음"
        else -> "보통"
    }
    val sensitivityFill = when (userProfile?.sensitivity) {
        CaffeineSensitivity.LOW -> 0.0f
        CaffeineSensitivity.SENSITIVE -> 1.0f
        else -> 0.5f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "내 신체 및 설정 정보",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )
                Text(
                    text = "체내 남은 카페인 계산 및 알림 기준이 됩니다.",
                    fontSize = 14.sp,
                    color = SnoffeeTextMuted
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    title = "신장",
                    value = displayHeight,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showHeightDialog = true }
                )
                InfoCard(
                    title = "체중",
                    value = displayWeight,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showWeightDialog = true }
                )
            }

            Box(modifier = Modifier.clickable { showSensitivityDialog = true }) {
                CaffeineSensitivityCard(
                    statusText = sensitivityText,
                    progressFill = sensitivityFill
                )
            }

            TimeSettingCard(
                title = "목표 수면 시간",
                time = displaySleepTime,
                iconRes = R.drawable.ic_main_bottombar_sleep,
                onClick = { showSleepTimePicker = true }
            )
            TimeSettingCard(
                title = "목표 기상 시간",
                time = displayWakeTime,
                iconRes = R.drawable.ic_setting_light_mode,
                onClick = { showWakeTimePicker = true }
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "앱 설정",
                    fontSize = 13.sp,
                    color = SnoffeeTextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SnoffeeSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        MenuRowItem(
                            title = "알림 설정",
                            iconRes = R.drawable.ic_setting_bell,
                            onClick = onNotificationSettingClick
                        )

                        MenuRowItem(
                            title = "건강데이터 및 기기연동",
                            iconRes = R.drawable.ic_setting_question,
                            onClick = onHelpClick
                        )
                    }
                }
            }
        }

        // 스낵바 오버레이 (기존 Scaffold snackbarHost 대체)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showHeightDialog) {
        var inputHeight by remember {
            mutableStateOf(
                userProfile?.height?.let {
                    if (it % 1.0 == 0.0) it.toInt().toString() else String.format(
                        Locale.US,
                        "%.1f",
                        it
                    )
                } ?: ""
            )
        }
        val decimalRegex = Regex("^\\d*\\.?\\d{0,1}$")
        val parsedHeight = inputHeight.toDoubleOrNull()
        val isHeightValid = parsedHeight != null && parsedHeight in 100.0..300.0
        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text(text = "신장 수정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputHeight,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(decimalRegex)) {
                            inputHeight = newValue
                        }
                    },
                    label = { Text("신장 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isHeightValid && inputHeight.isNotBlank(),
                    supportingText = {
                        if (!isHeightValid && inputHeight.isNotBlank()) {
                            Text(text = "100~300cm 사이로 입력해 주세요.", color = SnoffeeError)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        inputHeight.toDoubleOrNull()?.let { heightValue ->
                            //실패 시 팝업 유지 및 스낵바 전송
                            viewModel.updateHeight(heightValue) { success ->
                                if (success) {
                                    showHeightDialog = false
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("신장 정보 저장에 실패했습니다. 다시 시도해 주세요.")
                                    }
                                }
                            }
                        }
                    },
                    enabled = isHeightValid
                ) { Text("저장", color = SnoffeePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showHeightDialog = false }) {
                    Text(
                        "취소",
                        color = SnoffeeTextMuted
                    )
                }
            }
        )
    }

    if (showWeightDialog) {
        var inputWeight by remember {
            mutableStateOf(
                userProfile?.weight?.let {
                    if (it % 1.0 == 0.0) it.toInt().toString() else String.format(
                        Locale.US,
                        "%.1f",
                        it
                    )
                } ?: "")
        }
        val decimalRegex = Regex("^\\d*\\.?\\d{0,1}$")
        val parsedWeight = inputWeight.toDoubleOrNull()
        val isWeightValid = parsedWeight != null && parsedWeight in 1.0..400.0
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text(text = "체중 수정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(decimalRegex)) {
                            inputWeight = newValue
                        }
                    },
                    label = { Text("체중 (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = !isWeightValid && inputWeight.isNotBlank(),
                    supportingText = {
                        if (!isWeightValid && inputWeight.isNotBlank()) {
                            Text(text = "1~400kg 사이로 입력해 주세요.", color = SnoffeeError)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        inputWeight.toDoubleOrNull()?.let { weightValue ->
                            //실패 시 팝업 유지 및 스낵바 전송
                            viewModel.updateWeight(weightValue) { success ->
                                if (success) {
                                    showWeightDialog = false
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("체중 정보 저장에 실패했습니다. 다시 시도해 주세요.")
                                    }
                                }
                            }
                        }
                    },
                    enabled = isWeightValid
                ) { Text("저장", color = SnoffeePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text(
                        "취소",
                        color = SnoffeeTextMuted
                    )
                }
            }
        )
    }

    if (showSensitivityDialog) {
        val options = listOf("둔감함", "보통", "민감함")
        var selectedIndex by remember {
            mutableIntStateOf(
                when (userProfile?.sensitivity) {
                    CaffeineSensitivity.LOW -> 0
                    CaffeineSensitivity.SENSITIVE -> 2
                    else -> 1
                }
            )
        }
        AlertDialog(
            onDismissRequest = { showSensitivityDialog = false },
            title = { Text(text = "카페인 민감도 변경", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    options.forEachIndexed { index, text ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedIndex = index }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedIndex == index),
                                onClick = { selectedIndex = index },
                                colors = RadioButtonDefaults.colors(selectedColor = SnoffeePrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = text, color = SnoffeeTextMain, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val selectedSensitivity = when (selectedIndex) {
                        0 -> CaffeineSensitivity.LOW
                        2 -> CaffeineSensitivity.SENSITIVE
                        else -> CaffeineSensitivity.NORMAL
                    }
                    //실패 시 팝업 유지 및 스낵바 전송
                    viewModel.updateSensitivity(selectedSensitivity) { success ->
                        if (success) {
                            showSensitivityDialog = false
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("민감도 변경 사항을 저장하지 못했습니다.")
                            }
                        }
                    }
                }) { Text("적용", color = SnoffeePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showSensitivityDialog = false }) {
                    Text(
                        "취소",
                        color = SnoffeeTextMuted
                    )
                }
            }
        )
    }

    if (showSleepTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (rawSleep.length >= 4) {
                val parsedHour = rawSleep.substring(0, 2).toIntOrNull() ?: 22
                if (parsedHour < 12) parsedHour + 12 else parsedHour
            } else 22,
            initialMinute = if (rawSleep.length >= 4) rawSleep.substring(2, 4).toIntOrNull()
                ?: 30 else 30,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showSleepTimePicker = false },
            title = { Text(text = "목표 수면 시간 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format(
                        Locale.US,
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    //실패 시 팝업 유지 및 스낵바 전송
                    viewModel.updateSleepTime(formattedTime) { success ->
                        if (success) {
                            showSleepTimePicker = false
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("수면 시간 설정 저장에 실패했습니다.")
                            }
                        }
                    }
                }) { Text("변경", color = SnoffeePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showSleepTimePicker = false }) {
                    Text(
                        "취소",
                        color = SnoffeeTextMuted
                    )
                }
            }
        )
    }

    if (showWakeTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = if (rawWake.length >= 4) {
                val parsedHour = rawWake.substring(0, 2).toIntOrNull() ?: 6
                if (parsedHour >= 12) parsedHour - 12 else parsedHour
            } else 6,
            initialMinute = if (rawWake.length >= 4) rawWake.substring(2, 4).toIntOrNull()
                ?: 30 else 30,
            is24Hour = false
        )

        AlertDialog(
            onDismissRequest = { showWakeTimePicker = false },
            title = { Text(text = "목표 기상 시간 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val formattedTime = String.format(
                        Locale.US,
                        "%02d:%02d",
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    // 🌟 ViewModel 결과 관찰하여 실패 시 팝업 유지 및 스낵바 전송
                    viewModel.updateWakeTime(formattedTime) { success ->
                        if (success) {
                            showWakeTimePicker = false
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("기상 시간 설정 저장에 실패했습니다.")
                            }
                        }
                    }
                }) { Text("변경", color = SnoffeePrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showWakeTimePicker = false }) {
                    Text(
                        "취소",
                        color = SnoffeeTextMuted
                    )
                }
            }
        )
    }
}

@Composable
fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = title, fontSize = 14.sp, color = SnoffeeTextHint)
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeePrimaryDark
            )
        }
    }
}

@Composable
fun CaffeineSensitivityCard(statusText: String, progressFill: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "카페인 민감도",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SnoffeeTextMain
                )

                Surface(
                    color = SnoffeePrimarySubtle,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SnoffeePrimaryDark,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(SnoffeeBgBase, RoundedCornerShape(4.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFill)
                        .fillMaxHeight()
                        .background(SnoffeePrimary, RoundedCornerShape(4.dp))
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "둔감함", fontSize = 13.sp, color = SnoffeeTextHint)
                Text(text = "보통", fontSize = 13.sp, color = SnoffeeTextHint)
                Text(text = "민감함", fontSize = 13.sp, color = SnoffeeTextHint)
            }
        }
    }
}


@Composable
fun MenuRowItem(
    title: String,
    iconRes: Int,
    value: String? = null,
    isLogout: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isLogout) SnoffeeError else SnoffeeTextMain
            )
            Text(
                text = title,
                fontSize = 14.sp,
                color = if (isLogout) SnoffeeError else SnoffeeTextMain,
                fontWeight = if (isLogout) FontWeight.SemiBold else FontWeight.Normal
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(
                    text = value,
                    fontSize = 14.sp,
                    color = SnoffeeTextMuted,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            if (!isLogout) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_right),
                    contentDescription = null,
                    tint = SnoffeeTextDisabled,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun MenuRowItemWithSwitch(
    title: String,
    iconRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = SnoffeeTextMain
            )
            Text(text = title, fontSize = 14.sp, color = SnoffeeTextMain)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SnoffeePrimary,
                uncheckedThumbColor = SnoffeeTextDisabled,
                uncheckedTrackColor = SnoffeeBgMuted
            )
        )
    }
}