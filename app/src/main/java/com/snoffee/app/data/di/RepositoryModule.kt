package com.snoffee.app.data.di

import com.snoffee.app.data.repository.CaffeineRepositoryImpl
import com.snoffee.app.data.repository.DrinkRepositoryImpl
import com.snoffee.app.data.repository.GeminiRepositoryImpl
import com.snoffee.app.data.repository.SleepRepositoryImpl
import com.snoffee.app.data.repository.UserProfileRepositoryImpl
import com.snoffee.app.domain.repository.CaffeineRepository
import com.snoffee.app.domain.repository.DrinkRepository
import com.snoffee.app.domain.repository.GeminiRepository
import com.snoffee.app.domain.repository.SleepRepository
import com.snoffee.app.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Repository 인터페이스 → 구현체를 Hilt에 바인딩하는 모듈
// @Binds → domain/repository 인터페이스와 data/repository 구현체를 연결
// SingletonComponent → 앱 전체에서 하나의 인스턴스만 사용
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCaffeineRepository(
        impl: CaffeineRepositoryImpl
    ): CaffeineRepository

    @Binds
    @Singleton
    abstract fun bindSleepRepository(
        impl: SleepRepositoryImpl
    ): SleepRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        impl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindDrinkRepository(
        impl: DrinkRepositoryImpl
    ): DrinkRepository

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(
        impl: GeminiRepositoryImpl
    ): GeminiRepository
}