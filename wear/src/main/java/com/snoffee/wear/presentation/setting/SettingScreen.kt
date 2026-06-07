package com.snoffee.wear.presentation.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip

@Composable
fun SettingScreen(viewModel: SettingViewModel) {
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 40.dp, bottom = 40.dp)
    ) {
        item {
            Text("설정", style = MaterialTheme.typography.title3)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            ToggleChip(
                checked = viewModel.isNotificationEnabled.value,
                onCheckedChange = { viewModel.toggleNotification(it) },
                label = { Text("알림 수령") },
                toggleControl = {
                    Switch(checked = viewModel.isNotificationEnabled.value)
                }
            )
        }
        item {
            ToggleChip(
                checked = viewModel.isSleepModeEnabled.value,
                onCheckedChange = { viewModel.toggleSleepMode(it) },
                label = { Text("자동 다크 모드") },
                toggleControl = {
                    Switch(checked = viewModel.isSleepModeEnabled.value)
                }
            )
        }
    }
}