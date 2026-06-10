package com.snoffee.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.snoffee.app.service.PhoneNotificationHelper

class PhoneRemainderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val residual = intent.getDoubleExtra("RESIDUAL_AMOUNT", 0.0)
        PhoneNotificationHelper.sendPhoneNotification(context, 3002, false, residual)
    }
}