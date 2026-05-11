package com.snoffee.app.data.di

import android.content.Context
//import com.samsung.android.sdk.health.data.HealthDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// 삼성헬스 SDK 인스턴스를 Hilt에 등록하는 모듈
// SingletonComponent → 앱 전체에서 하나의 삼성헬스 인스턴스만 사용
// SDK 버전 업데이트 시 이 파일만 수정하면 됨
@Module
@InstallIn(SingletonComponent::class)
object HealthModule {

    // todo :: 삼성 공식 SDK 필요
//    @Provides
//    @Singleton
//    fun provideHealthDataService(
//        @ApplicationContext context: Context  // Hilt가 자동으로 Context 주입
//    ): HealthDataService {
//        return HealthDataService.getClient(context)
//    }
}