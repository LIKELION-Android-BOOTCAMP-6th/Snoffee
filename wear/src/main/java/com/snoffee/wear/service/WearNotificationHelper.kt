package com.snoffee.wear.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.snoffee.wear.R

object WearNotificationHelper {

    private const val CHANNEL_ID = "CAFFEINE_CHANNEL"
    private const val NOTIFICATION_ID = 1

    // 알림 채널 초기화 (앱 시작 or 필요시 호출)
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "카페인 컷오프 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "취침 전 카페인 섭취 알림 채널"
            }
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    // 알림 발송
    fun sendCaffeineCutoffNotification(context: Context, residual: Double) {
        // 안드로이드 13 이상 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 프로젝트의 알림 아이콘으로 확인 필요
            .setContentTitle("카페인 컷오프 알림")
            .setContentText("현재 잔류 카페인이 ${residual.toInt()}mg입니다. 수면을 위해 섭취를 멈춰주세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}