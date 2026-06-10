package com.snoffee.wear.service

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.snoffee.wear.R
import com.snoffee.wear.receiver.CutoffAlarmReceiver
import com.snoffee.wear.receiver.NotificationDismissReceiver
import com.snoffee.wear.receiver.RemainderAlarmReceiver
import kotlin.math.ln

object WearNotificationHelper {

    // 완료 기준 - 컷오프(긴급) 및 취침 전(일반) 고유 채널 ID 정의
    private const val CHANNEL_CUTOFF_ID = "CUTOFF_EMERGENCY_CHANNEL"
    private const val CHANNEL_REMAINDER_ID = "REMAINDER_GENERAL_CHANNEL"

    // 패턴 정의
    private val CUTOFF_PATTERN = longArrayOf(0, 150, 100, 150)
    private val REMAINDER_PATTERN = longArrayOf(0, 200, 150, 200, 150, 200)

    // 알림 채널 분리 생성
    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val cutoffChannel = NotificationChannel(
            CHANNEL_CUTOFF_ID,
            "긴급 컷오프 경고",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "수면 방해 위험 알림"
            enableVibration(false)
        }

        val remainderChannel = NotificationChannel(
            CHANNEL_REMAINDER_ID,
            "잔류량 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "취침 전 카페인 잔류량 정보"
            enableVibration(false)
        }

        manager.createNotificationChannel(cutoffChannel)
        manager.createNotificationChannel(remainderChannel)
    }

    // 스마트 알림 메시지, 커스텀 스타일 바인딩 및 발송
    fun sendCaffeineNotification(
        context: Context,
        id: Int,
        isCutoff: Boolean,
        residual: Double,
        isNotificationEnabled: Boolean
    ) {
        if (!isNotificationEnabled) return

        // 안드로이드 13 이상 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val channelId = if (isCutoff) CHANNEL_CUTOFF_ID else CHANNEL_REMAINDER_ID
        val title = if (isCutoff) "⚠️ 카페인 컷오프 경고" else "🌙 취침 전 잔류량 안내"
        val content = if (isCutoff) {
            "현재 잔류 카페인이 ${residual.toInt()}mg! 숙면을 위해 섭취를 멈춰주세요!"
        } else {
            "현재 잔류 카페인은 ${residual.toInt()}mg. 안락한 수면을 준비하세요."
        }

        // 닫기 버튼 클릭 대응
        val dismissIntent = Intent(context, NotificationDismissReceiver::class.java).apply {
            putExtra("NOTIFICATION_ID", id)
        }
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            id,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 워치 알림창 내 커스텀 스타일 바인딩
        val wearableExtender = NotificationCompat.WearableExtender()
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "닫기",
                    dismissPendingIntent
                ).build()
            )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(if (isCutoff) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setDeleteIntent(dismissPendingIntent) // 스와이프해서 지울 때도 폰-워치 동시 해제 트리거
            .extend(wearableExtender)

        // 알림 발송과 동시에 커스텀 진동 패턴 트리거
        NotificationManagerCompat.from(context).notify(id, builder.build())

        triggerVibration(context, isCutoff)
    }

    // 워치 단독 + 진동 모드에서의 진동패턴
    //예외 처리 : DND, 무음, 진폭 제어 미지원 하드웨어
    private fun triggerVibration(context: Context, isCutoff: Boolean) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 시스템이 설정한 방해금지(DND) 모드 체크
        val isDndActive =
            notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

        // 무음 모드 체크
        val isSilent = audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT

        // 만약 위 상태 중 하나라도 해당하면 진동을 발생시키지 않고 종료 (알림만 뜸)
        if (isDndActive || isSilent) return

        // 진동 실행
        // NotificationChannel 설정을 따르는 것이 원칙이나, 커스텀 패턴을 명시적으로 제어
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (vibrator.hasVibrator()) {
            val pattern = if (isCutoff) CUTOFF_PATTERN else REMAINDER_PATTERN
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    fun scheduleCutoffAlarm(
        context: Context,
        currentResidual: Double,
        halfLifeHours: Double,
        targetBedTimeMillis: Long,
        isNotificationEnabled: Boolean
    ) {
        if (!isNotificationEnabled) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTimeMillis = System.currentTimeMillis()


        val oneHourBeforeBedMillis = targetBedTimeMillis - (60 * 60 * 1000) // 취침 시간 - 1시간

        // 취침 1시간 전 알림
        if (oneHourBeforeBedMillis > currentTimeMillis) {
            val remainderIntent = Intent(context, RemainderAlarmReceiver::class.java).apply {
                putExtra("RESIDUAL_AMOUNT", currentResidual)
            }
            val remainderPendingIntent = PendingIntent.getBroadcast(
                context,
                2002,
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

        // 섭취 마지노선 알림
        if (currentResidual <= 50.0) return

        // 50mg까지 감소하는 데 걸리는 시간(시간 단위) 수학 공식 역산
        val hoursToDropTo50 = halfLifeHours * (ln(currentResidual / 50.0) / ln(2.0))
        val millisToDropTo50 = (hoursToDropTo50 * 60 * 60 * 1000).toLong()

        // 섭취 마지노선 시간 = 목표 취침 시간 - 소요 시간
        val cutoffTimeMillis = targetBedTimeMillis - millisToDropTo50

        // 만약 계산된 마지노선 시간이 이미 지나버렸다면 즉시 경고 발생
        if (cutoffTimeMillis <= currentTimeMillis) {
            sendCaffeineNotification(context, 1001, true, currentResidual, true)
            return
        }

        // 특정 시간에 정확히 알림을 깨울 AlarmManager 설정
        val intent = Intent(context, CutoffAlarmReceiver::class.java).apply {
            putExtra("RESIDUAL_AMOUNT", currentResidual)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Doze(절전) 모드에서도 정확하게 깨어나도록 설정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cutoffTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                cutoffTimeMillis,
                pendingIntent
            )
        }
    }
}