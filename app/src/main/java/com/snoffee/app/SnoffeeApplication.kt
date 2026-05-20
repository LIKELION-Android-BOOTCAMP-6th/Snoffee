package com.snoffee.app

import android.app.Application
import com.snoffee.app.data.initializer.CaffeineDataInitializer
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class SnoffeeApplication : Application() {
    // Application은 앱이 실행될 때 단 한 번만 생성됩니다
    @Inject
    lateinit var initializer: CaffeineDataInitializer

    override fun onCreate() {
        super.onCreate()
        // 앱 시작 시점에 CoroutineScope를 통해 음료 데이터 roomDB 저장 (캐싱)
        CoroutineScope(Dispatchers.IO).launch {
            initializer.initializeIfEmpty()
        }
    }
}