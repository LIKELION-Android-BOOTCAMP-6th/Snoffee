package com.snoffee.app.presentation.notification

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.snoffee.app.R
import com.snoffee.app.receiver.PhoneCutoffReceiver
import com.snoffee.app.receiver.PhoneRemainderReceiver
import kotlin.math.ln

class NotificationHelper(
    private val context: Context
) {
    companion object {
        private const val CHANNEL_CUTOFF_ID = "phone_cutoff_channel"
        private const val CHANNEL_REMAINDER_ID = "phone_remainder_channel"

        private const val CUTOFF_NOTIFICATION_ID = 3001
        private const val REMAINDER_NOTIFICATION_ID = 3002
    }

    init {
        createNotificationChannels()
    }

    @SuppressLint("MissingPermission")
    fun sendCaffeineNotification(
        id: Int,
        isCutoff: Boolean,
        residual: Double
    ) {
        if (!hasNotificationPermission()) return

        val channelId = if (isCutoff) CHANNEL_CUTOFF_ID else CHANNEL_REMAINDER_ID
        val title = if (isCutoff) {
            "⚠️ 카페인 컷오프 경고"
        } else {
            "🌙 취침 전 잔류량 안내"
        }

        val content = if (isCutoff) {
            if (residual > 150.0) {
                "카페인 누적량 ${residual.toInt()}mg 경고! 지금 이후로 추가 섭취는 피해주세요."
            } else {
                "현재 잔류 카페인이 ${residual.toInt()}mg입니다. 숙면을 위해 섭취를 멈춰주세요."
            }
        } else {
            "현재 잔류 카페인은 ${residual.toInt()}mg입니다. 수면을 준비해보세요."
        }

        val mainIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            id + 100,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(
                if (isCutoff) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setVibrate(
                if (isCutoff) longArrayOf(0, 500, 200, 500)
                else longArrayOf(0, 300, 200, 300)
            )
            .build()

        NotificationManagerCompat.from(context).notify(id, notification)
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleCaffeineAlarms(
        currentResidual: Double,
        halfLifeHours: Double,
        targetBedTimeMillis: Long,
        isNotificationEnabled: Boolean
    ) {
        if (!isNotificationEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTimeMillis = System.currentTimeMillis()

        scheduleRemainderAlarm(
            alarmManager = alarmManager,
            currentResidual = currentResidual,
            targetBedTimeMillis = targetBedTimeMillis,
            currentTimeMillis = currentTimeMillis
        )

        scheduleCutoffAlarm(
            alarmManager = alarmManager,
            currentResidual = currentResidual,
            halfLifeHours = halfLifeHours,
            targetBedTimeMillis = targetBedTimeMillis,
            currentTimeMillis = currentTimeMillis
        )
    }

    private fun scheduleRemainderAlarm(
        alarmManager: AlarmManager,
        currentResidual: Double,
        targetBedTimeMillis: Long,
        currentTimeMillis: Long
    ) {
        val oneHourBeforeBedMillis = targetBedTimeMillis - 60 * 60 * 1000

        if (oneHourBeforeBedMillis <= currentTimeMillis) return

        val intent = Intent(context, PhoneRemainderReceiver::class.java).apply {
            putExtra("RESIDUAL_AMOUNT", currentResidual)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REMAINDER_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, oneHourBeforeBedMillis, pendingIntent)
    }

    private fun scheduleCutoffAlarm(
        alarmManager: AlarmManager,
        currentResidual: Double,
        halfLifeHours: Double,
        targetBedTimeMillis: Long,
        currentTimeMillis: Long
    ) {
        if (currentResidual <= 50.0) return

        val hoursToDropTo50 = halfLifeHours * (ln(currentResidual / 50.0) / ln(2.0))
        val millisToDropTo50 = (hoursToDropTo50 * 60 * 60 * 1000).toLong()
        val cutoffTimeMillis = targetBedTimeMillis - millisToDropTo50

        if (cutoffTimeMillis <= currentTimeMillis) {
            sendCaffeineNotification(CUTOFF_NOTIFICATION_ID, true, currentResidual)
            return
        }

        val intent = Intent(context, PhoneCutoffReceiver::class.java).apply {
            putExtra("RESIDUAL_AMOUNT", currentResidual)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            CUTOFF_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        setAlarm(alarmManager, cutoffTimeMillis, pendingIntent)
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun setAlarm(
        alarmManager: AlarmManager,
        triggerAtMillis: Long,
        pendingIntent: PendingIntent
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

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
}