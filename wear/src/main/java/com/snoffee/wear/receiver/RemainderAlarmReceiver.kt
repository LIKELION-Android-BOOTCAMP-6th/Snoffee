package com.snoffee.wear.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.snoffee.wear.service.WearNotificationHelper

class RemainderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val residual = intent.getDoubleExtra("RESIDUAL_AMOUNT", 0.0)

        // 취침 1시간 전 일반 잔류량 안내 알림 발송 (isCutoff = false)
        WearNotificationHelper.sendCaffeineNotification(
            context = context,
            id = 1002, // 일반 알림 ID
            isCutoff = false,
            residual = residual,
            isNotificationEnabled = true
        )
    }
}