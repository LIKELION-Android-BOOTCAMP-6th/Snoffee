package com.snoffee.wear.service

import android.Manifest
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
import com.snoffee.wear.receiver.NotificationDismissReceiver

object WearNotificationHelper {

    // 완료 기준 - 컷오프(긴급) 및 취침 전(일반) 고유 채널 ID 정의
    private const val CHANNEL_CUTOFF_ID = "CUTOFF_EMERGENCY_CHANNEL"
    private const val CHANNEL_REMAINDER_ID = "REMAINDER_GENERAL_CHANNEL"

    // 알림 채널 분리 생성
    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val cutoffChannel = NotificationChannel(
            CHANNEL_CUTOFF_ID,
            "긴급 컷오프 경고",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "카페인 컷오프 진입 전 긴급 경고를 알립니다."
            enableVibration(false)
        }

        val remainderChannel = NotificationChannel(
            CHANNEL_REMAINDER_ID,
            "잔류량 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "취침 전 카페인 잔류량 정보를 안내합니다."
            enableVibration(false)
        }

        manager.createNotificationChannel(cutoffChannel)
        manager.createNotificationChannel(remainderChannel)
    }

    // 스마트 알림 메시지, 커스텀 스타일 바인딩 및 발송
    fun sendCaffeineNotification(context: Context, id: Int, isCutoff: Boolean, residual: Double) {
        // 안드로이드 13 이상 권한 체크
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val channelId = if (isCutoff) CHANNEL_CUTOFF_ID else CHANNEL_REMAINDER_ID
        val title = if (isCutoff) "⚠️ 카페인 컷오프 경고" else "🌙 취침 전 잔류량 안내"
        val content = if (isCutoff) {
            "현재 잔류 카페인이 ${residual.toInt()}mg입니다. 수면을 위해 섭취를 멈춰주세요!"
        } else {
            "현재 잔류 카페인은 ${residual.toInt()}mg입니다. 안락한 수면을 준비하세요."
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

        with(NotificationManagerCompat.from(context)) {
            notify(id, builder.build())
        }

        // 알림 발송과 동시에 커스텀 진동 패턴 트리거
        triggerVibration(context, isCutoff)
    }

    // 워치 단독 + 진동 모드에서의 진동패턴
    //예외 처리 : DND, 무음, 진폭 제어 미지원 하드웨어
    private fun triggerVibration(context: Context, isCutoff: Boolean) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (!vibrator.hasVibrator()) return

        val isDndActive =
            notificationManager.currentInterruptionFilter >= NotificationManager.INTERRUPTION_FILTER_PRIORITY
        if (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT || isDndActive) {
            return
        }

        if (isCutoff) {
            if (vibrator.hasAmplitudeControl()) {
                val timings = longArrayOf(0, 150, 100, 150)
                val amplitudes = intArrayOf(0, 255, 0, 255)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 150), -1))
            }
        } else {
            if (vibrator.hasAmplitudeControl()) {
                val timings = longArrayOf(0, 200, 150, 200, 150, 200)
                val amplitudes = intArrayOf(0, 100, 0, 100, 0, 100)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(
                            0,
                            250,
                            150,
                            250,
                            150,
                            250
                        ), -1
                    )
                )
            }
        }
    }
}