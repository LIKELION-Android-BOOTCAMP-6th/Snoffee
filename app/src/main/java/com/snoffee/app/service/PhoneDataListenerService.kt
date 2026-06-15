package com.snoffee.app.service

import android.util.Log
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.snoffee.app.data.wear.PhoneDataClient
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.CaffeineSensitivity
import com.snoffee.app.domain.model.UserProfile
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.ln
import kotlin.math.pow

@AndroidEntryPoint
class PhoneDataListenerService : WearableListenerService() {

    @Inject
    lateinit var caffeineRepository: CaffeineRepository

    @Inject
    lateinit var phoneDataClient: PhoneDataClient

    @Inject
    lateinit var userProfileRepository: UserProfileRepository

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e("SnoffeeError", "💥 코루틴 에러: ${throwable.localizedMessage}", throwable)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            val uriPath = event.dataItem.uri.path ?: continue
            if (uriPath.endsWith("/caffeine/add_record")) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val name = dataMap.getString("name") ?: "알 수 없음"
                val amount = dataMap.getInt("amount")
                val consumedAt = dataMap.getLong("timestamp")

                CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                    caffeineRepository.processAndInsertCaffeine(name, amount, consumedAt)
                    Log.d("SnoffeeSave", "✍️ DB 적재 성공: $name, $amount mg, 시간: $consumedAt")
                    syncWithWatch(
                        consumedAt,
                        caffeineRepository.getCaffeineRecordsSince(consumedAt - 86400000L).first()
                    )
                }
            }
        }
    }

    // 메시지 수신 오버라이드 추가
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/caffeine/request_sync") {
            CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
                val currentTime = System.currentTimeMillis()
                val since24HoursAgo = currentTime - (24 * 60 * 60 * 1000L)
                val recentRecords =
                    caffeineRepository.getCaffeineRecordsSince(since24HoursAgo).first()
                syncWithWatch(currentTime, recentRecords)

                Log.d("SnoffeeDebug", "📱 워치 초기 요청 확인 -> 즉시 상태 동기화 완료")
            }
        }
    }

    private suspend fun syncWithWatch(currentTime: Long, recentRecords: List<CaffeineRecord>) {
        val profile = userProfileRepository.getUserProfile()
            ?: UserProfile(1, 70.0, 175.0, 400.0, false, 0L, 0L, CaffeineSensitivity.NORMAL, 0L)
        val halfLifeHours = profile.sensitivity.halfLifeHours
        val halfLifeMillis = halfLifeHours * 60 * 60 * 1000L

        var totalResidualMg = 0.0
        recentRecords.forEach { record ->
            val timeDiff = currentTime - record.consumedAt
            if (timeDiff > 0) {
                val residual =
                    record.intakeCaffeine * (0.5).pow(timeDiff.toDouble() / halfLifeMillis)
                totalResidualMg += residual
            }
        }

        val riskLevel = when {
            totalResidualMg < 150.0 -> "SAFE"
            totalResidualMg < 300.0 -> "CAUTION"
            else -> "DANGER"
        }

        val metabolismTimeStr = if (totalResidualMg > 5.0) {
            val remainingHours = -(halfLifeHours * ln(5.0 / totalResidualMg)) / ln(2.0)
            val completionMillis = currentTime + (remainingHours * 60 * 60 * 1000L).toLong()
            SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(completionMillis))
        } else {
            "--:--"
        }

        phoneDataClient.sendCaffeineStateToWatch(
            residualMg = totalResidualMg,
            riskLevel = riskLevel,
            metabolismTime = metabolismTimeStr,
            concentrationLevel = if (totalResidualMg > 150.0) "높음" else "보통",
            sensitivity = profile.sensitivity.name,
            targetSleepTime = profile.userSleepTime
        )
    }
}