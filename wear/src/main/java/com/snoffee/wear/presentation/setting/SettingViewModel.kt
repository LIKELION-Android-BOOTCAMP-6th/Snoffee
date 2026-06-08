package com.snoffee.wear.presentation.setting

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.snoffee.wear.data.WearDataClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(private val wearDataClient: WearDataClient) :
    ViewModel() {
    val isNotificationEnabled = mutableStateOf(true)
    val isSleepModeEnabled = mutableStateOf(false)

    fun toggleNotification(enabled: Boolean) {
        isNotificationEnabled.value = enabled
        wearDataClient.sendMessage("/settings/notification", enabled.toString().toByteArray())
    }

    fun toggleSleepMode(enabled: Boolean) {
        isSleepModeEnabled.value = enabled
        wearDataClient.sendMessage("/settings/sleep", enabled.toString().toByteArray())
    }
}