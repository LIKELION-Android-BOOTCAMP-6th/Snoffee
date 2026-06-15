//package com.snoffee.wear.presentation.setting
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.compose.runtime.mutableStateOf
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.ViewModel
//import com.snoffee.wear.data.WearDataClient
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class SettingViewModel @Inject constructor(private val wearDataClient: WearDataClient) :
//    ViewModel() {
//    val isNotificationEnabled = mutableStateOf(true)
//    val isSleepModeEnabled = mutableStateOf(false)
//    val isSystemNotificationPermissionGranted = mutableStateOf(true)
//
//    fun toggleNotification(enabled: Boolean) {
//        isNotificationEnabled.value = enabled
//        wearDataClient.sendMessage("/settings/notification", enabled.toString().toByteArray())
//    }
//
//    fun toggleSleepMode(enabled: Boolean) {
//        isSleepModeEnabled.value = enabled
//        wearDataClient.sendMessage("/settings/sleep", enabled.toString().toByteArray())
//    }
//
//    fun checkSystemNotificationPermission(context: Context) {
//        isSystemNotificationPermissionGranted.value =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                ContextCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.POST_NOTIFICATIONS
//                ) == PackageManager.PERMISSION_GRANTED
//            } else {
//                true
//            }
//    }
//}