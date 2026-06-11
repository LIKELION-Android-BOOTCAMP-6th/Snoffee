package com.snoffee.wear.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.snoffee.wear.service.WearNotificationHelper

class CutoffAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val residual = intent.getDoubleExtra("RESIDUAL_AMOUNT", 0.0)

        // 마지노선 시간이 되었으므로 유저에게 긴급 컷오프 경고 알림 발송
        WearNotificationHelper.sendCaffeineNotification(
            context = context,
            id = 1001,
            isCutoff = true,
            residual = residual,
            isNotificationEnabled = true
        )
    }
}