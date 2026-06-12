//package com.snoffee.wear.presentation.setting
//
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
//import androidx.wear.compose.material.MaterialTheme
//import androidx.wear.compose.material.Switch
//import androidx.wear.compose.material.Text
//import androidx.wear.compose.material.ToggleChip
//
//@Composable
//fun SettingScreen(viewModel: SettingViewModel) {
//    val context = LocalContext.current
//    val listState = rememberScalingLazyListState()
//
//    LaunchedEffect(Unit) {
//        viewModel.checkSystemNotificationPermission(context)
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(horizontal = 8.dp),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("설정", style = MaterialTheme.typography.title3)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        ToggleChip(
//            checked = viewModel.isNotificationEnabled.value,
//            onCheckedChange = { viewModel.toggleNotification(it) },
//            label = { Text("알림 수령") },
//            toggleControl = {
//                Switch(checked = viewModel.isNotificationEnabled.value)
//            }
//        )
//
//        if (!viewModel.isSystemNotificationPermissionGranted.value) {
//            Spacer(modifier = Modifier.height(2.dp))
//            Text(
//                text = "⚠️ 알림 권한이 차단되어 있습니다.",
//                style = MaterialTheme.typography.caption3,
//                color = MaterialTheme.colors.error
//            )
//        }
//        Spacer(modifier = Modifier.height(4.dp))
//        ToggleChip(
//            checked = viewModel.isSleepModeEnabled.value,
//            onCheckedChange = { viewModel.toggleSleepMode(it) },
//            label = { Text("자동 다크 모드") },
//            toggleControl = {
//                Switch(checked = viewModel.isSleepModeEnabled.value)
//            }
//        )
//    }
//}