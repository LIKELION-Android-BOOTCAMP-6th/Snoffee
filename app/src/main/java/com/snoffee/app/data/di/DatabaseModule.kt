package com.snoffee.app.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

// Room DB 인스턴스를 Hilt에 등록하는 모듈
// SingletonComponent → 앱 전체에서 하나의 DB 인스턴스만 사용
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    // TODO: Room DB 설정 후 작업 필요
}