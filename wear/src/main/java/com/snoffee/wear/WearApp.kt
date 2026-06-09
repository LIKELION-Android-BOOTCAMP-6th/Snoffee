package com.snoffee.wear

import android.app.Application
import com.snoffee.wear.service.WearNotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // 앱 초기 실행 환경 시 채널 인프라 자동 빌드업
        WearNotificationHelper.createNotificationChannels(this)
    }
}