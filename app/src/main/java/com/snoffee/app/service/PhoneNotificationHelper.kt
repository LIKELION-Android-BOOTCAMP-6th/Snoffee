package com.snoffee.app.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.snoffee.app.R
import com.snoffee.app.receiver.PhoneCutoffReceiver
import com.snoffee.app.receiver.PhoneRemainderReceiver
import kotlin.math.ln

object PhoneNotificationHelper {

    private const val CHANNEL_CUTOFF_ID = "PHONE_CUTOFF_CHANNEL"
    private const val CHANNEL_REMAINDER_ID = "PHONE_REMAINDER_CHANNEL"

    // 폰 앱 알림 채널 생성 (MainActivity의 onCreate 등에서 최초 1회 호출 필요)
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val cutoffChannel = NotificationChannel(
                CHANNEL_CUTOFF_ID,
                "카페인 컷오프 경고",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "수면 방해 위험 누적 알림"
            }

            val remainderChannel = NotificationChannel(
                CHANNEL_REMAINDER_ID,
                "취침 전 잔류량 안내",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "취침 전 카페인 상태 정보"
            }

            manager.createNotificationChannel(cutoffChannel)
            manager.createNotificationChannel(remainderChannel)
        }
    }

    // 실제 알림 발송 로직
    fun sendPhoneNotification(context: Context, id: Int, isCutoff: Boolean, residual: Double) {
        val channelId = if (isCutoff) CHANNEL_CUTOFF_ID else CHANNEL_REMAINDER_ID
        val title = if (isCutoff) "⚠️ 카페인 컷오프 경고" else "🌙 취침 전 잔류량 안내"

        val content = if (isCutoff) {
            if (residual > 150.0) {
                "카페인 누적량 ${residual.toInt()}mg 경고! 지금 이후로 추가 섭취는 절대 금지입니다!"
            } else {
                "현재 잔류 카페인이 ${residual.toInt()}mg! 숙면을 위해 섭취를 멈춰주세요!"
            }
        } else {
            "현재 잔류 카페인은 ${residual.toInt()}mg. 안락한 수면을 준비하세요."
        }

        // 알림 클릭 시 앱 메인 화면으로 이동하도록 인텐트 설정
        val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            id + 100,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 폰 앱 고유 아이콘으로 변경 가능
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(if (isCutoff) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVibrate(
                if (isCutoff) longArrayOf(0, 500, 200, 500) else longArrayOf(
                    0,
                    300,
                    200,
                    300
                )
            ) // 폰 전용 진동

        // 권한 체크는 호출부(ViewModel/Activity)나 Android 13 대응 Launcher에서 처리됨을 전제
        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // 알람 스케줄링 통합 로직
    @SuppressLint("ScheduleExactAlarm")
    fun schedulePhoneAlarms(
        context: Context,
        currentResidual: Double,
        halfLifeHours: Double,
        targetBedTimeMillis: Long,
        isNotificationEnabled: Boolean
    ) {
        if (!isNotificationEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTimeMillis = System.currentTimeMillis()

        // 1️⃣ [시나리오 A] 취침 1시간 전 안내 예약
        val oneHourBeforeBedMillis = targetBedTimeMillis - (60 * 60 * 1000)
        if (oneHourBeforeBedMillis > currentTimeMillis) {
            val remainderIntent = Intent(context, PhoneRemainderReceiver::class.java).apply {
                putExtra("RESIDUAL_AMOUNT", currentResidual)
            }
            val remainderPendingIntent = PendingIntent.getBroadcast(
                context,
                3002,
                remainderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    oneHourBeforeBedMillis,
                    remainderPendingIntent
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    oneHourBeforeBedMillis,
                    remainderPendingIntent
                )
            }
        }

        // 2️⃣ [시나리오 B] 누적 기반 마지노선 컷오프 예약
        if (currentResidual <= 50.0) return

        val hoursToDropTo50 = halfLifeHours * (ln(currentResidual / 50.0) / ln(2.0))
        val millisToDropTo50 = (hoursToDropTo50 * 60 * 60 * 1000).toLong()
        val cutoffTimeMillis = targetBedTimeMillis - millisToDropTo50

        if (cutoffTimeMillis <= currentTimeMillis) {
            sendPhoneNotification(context, 3001, true, currentResidual)
            return
        }

        val cutoffIntent = Intent(context, PhoneCutoffReceiver::class.java).apply {
            putExtra("RESIDUAL_AMOUNT", currentResidual)
        }
        val cutoffPendingIntent = PendingIntent.getBroadcast(
            context,
            3001,
            cutoffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cutoffTimeMillis,
                cutoffPendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cutoffTimeMillis,
                cutoffPendingIntent
            )
        }
    }
}