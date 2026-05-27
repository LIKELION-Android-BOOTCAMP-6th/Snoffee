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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    onNotificationSettingClick: () -> Unit = {},
    onLanguageSettingClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    viewModel: SettingViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    var isDarkMode by remember { mutableStateOf(false) }

    val userProfile by viewModel.userProfile.collectAsState()

    var showHeightDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showSensitivityDialog by remember { mutableStateOf(false) }
    var showSleepTimePicker by remember { mutableStateOf(false) }
    var showWakeTimePicker by remember { mutableStateOf(false) }

    val displayHeight = userProfile?.height?.let { "${it.toInt()}cm" } ?: "-cm"
    val displayWeight = userProfile?.weight?.let { "${it.toInt()}kg" } ?: "-kg"

    val rawSleep = userProfile?.userSleepTime?.toString() ?: "2230"
    val displaySleepTime = if (rawSleep.length >= 4) "${rawSleep.substring(0, 2)}:${
        rawSleep.substring(
            2,
            4
        )
    }" else "22:30"

    val rawWake = userProfile?.wakeTime?.toString() ?: "0630"
    val displayWakeTime = if (rawWake.length >= 4) "${rawWake.substring(0, 2)}:${
        rawWake.substring(
            2,
            4
        )
    }" else "06:30"

    val sensitivityText = when (userProfile?.sensitivity) {
        CaffeineSensitivity.LOW -> "낮음"
        CaffeineSensitivity.SENSITIVE -> "높음"
        else -> "보통"
    }
    val sensitivityFill = when (userProfile?.sensitivity) {
        CaffeineSensitivity.LOW -> 0.1f
        CaffeineSensitivity.SENSITIVE -> 1.0f
        else -> 0.5f
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SnoffeeBgBase)
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
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = SnoffeeTextMain
            )
            Text(
                text = "체내 남은 카페인 계산 및 알림 기준이 됩니다.",
                fontSize = 13.sp,
                color = SnoffeeTextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

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
            CaffeineSensitivityCard(statusText = sensitivityText, progressFill = sensitivityFill)
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

                    MenuRowItemWithSwitch(
                        title = "다크 모드",
                        iconRes = R.drawable.ic_main_bottombar_sleep,
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    )

                    MenuRowItem(
                        title = "언어 설정",
                        value = "한국어",
                        iconRes = R.drawable.ic_setting_language,
                        onClick = onLanguageSettingClick
                    )

                    MenuRowItem(
                        title = "도움말",
                        iconRes = R.drawable.ic_setting_question,
                        onClick = onHelpClick
                    )
                }
            }
        }
    }

    if (showHeightDialog) {
        val initialHeight = userProfile?.height?.toInt()?.toString() ?: ""
        var inputHeight by remember { mutableStateOf(initialHeight) }

        AlertDialog(
            onDismissRequest = { showHeightDialog = false },
            title = { Text(text = "신장 수정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputHeight,
                    onValueChange = { inputHeight = it },
                    label = { Text("신장 (cm)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    inputHeight.toDoubleOrNull()?.let { viewModel.updateHeight(it) }
                    showHeightDialog = false
                }) { Text("저장", color = SnoffeePrimary) }
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
        val initialWeight = userProfile?.weight?.toInt()?.toString() ?: ""
        var inputWeight by remember { mutableStateOf(initialWeight) }

        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text(text = "체중 수정", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = inputWeight,
                    onValueChange = { inputWeight = it },
                    label = { Text("체중 (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    inputWeight.toDoubleOrNull()?.let { viewModel.updateWeight(it) }
                    showWeightDialog = false
                }) { Text("저장", color = SnoffeePrimary) }
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
        val currentInitialIndex = when (userProfile?.sensitivity) {
            CaffeineSensitivity.LOW -> 0
            CaffeineSensitivity.SENSITIVE -> 2
            else -> 1
        }
        var selectedIndex by remember { mutableIntStateOf(currentInitialIndex) }

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
                    showSensitivityDialog = false
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
        val parsedHour =
            if (rawSleep.length >= 4) rawSleep.substring(0, 2).toIntOrNull() ?: 22 else 22
        val initialMinute =
            if (rawSleep.length >= 4) rawSleep.substring(2, 4).toIntOrNull() ?: 30 else 30

        val initialHour = if (parsedHour < 12) parsedHour + 12 else parsedHour

        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
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
                    viewModel.updateSleepTime(formattedTime)
                    showSleepTimePicker = false
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
        val parsedHour = if (rawWake.length >= 4) rawWake.substring(0, 2).toIntOrNull() ?: 6 else 6
        val initialMinute =
            if (rawWake.length >= 4) rawWake.substring(2, 4).toIntOrNull() ?: 30 else 30

        val initialHour = if (parsedHour >= 12) parsedHour - 12 else parsedHour

        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
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
                    viewModel.updateWakeTime(formattedTime)
                    showWakeTimePicker = false
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
            Text(text = title, fontSize = 13.sp, color = SnoffeeTextHint)
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
                Text(text = "둔감함", fontSize = 11.sp, color = SnoffeeTextHint)
                Text(text = "보통", fontSize = 11.sp, color = SnoffeeTextHint)
                Text(text = "민감함", fontSize = 11.sp, color = SnoffeeTextHint)
            }
        }
    }
}

@Composable
fun TimeSettingCard(title: String, time: String, iconRes: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SnoffeeSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SnoffeeBgBase, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = SnoffeePrimary
                    )
                }
                Column {
                    Text(text = title, fontSize = 12.sp, color = SnoffeeTextHint)
                    Text(
                        text = time,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnoffeeTextMain
                    )
                }
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = SnoffeeTextDisabled,
                modifier = Modifier.size(18.dp)
            )
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