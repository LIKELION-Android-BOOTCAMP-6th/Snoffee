package com.snoffee.app.data.di

import com.snoffee.app.data.datasource.local.CaffeineLocalDataSource
import com.snoffee.app.data.datasource.local.CaffeineLocalDataSourceImpl
import com.snoffee.app.data.datasource.local.DrinkLocalDataSource
import com.snoffee.app.data.datasource.local.DrinkLocalDataSourceImpl
import com.snoffee.app.data.datasource.local.UserProfileLocalDataSource
import com.snoffee.app.data.datasource.local.UserProfileLocalDataSourceImpl
import com.snoffee.app.data.datasource.health.SamsungHealthDataSource
import com.snoffee.app.data.datasource.health.SamsungHealthDataSourceImpl
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSource
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSourceImpl
import com.snoffee.app.data.datasource.remote.GeminiRemoteDataSource
import com.snoffee.app.data.datasource.remote.GeminiRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// DataSource 인터페이스 → 구현체를 Hilt에 바인딩하는 모듈
// @Binds → 인터페이스와 구현체를 연결할 때 사용
// SingletonComponent → 앱 전체에서 하나의 인스턴스만 사용
@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    // Local DataSource
    @Binds
    @Singleton
    abstract fun bindCaffeineLocalDataSource(
        impl: CaffeineLocalDataSourceImpl
    ): CaffeineLocalDataSource

    @Binds
    @Singleton
    abstract fun bindDrinkLocalDataSource(
        impl: DrinkLocalDataSourceImpl
    ): DrinkLocalDataSource

    @Binds
    @Singleton
    abstract fun bindUserProfileLocalDataSource(
        impl: UserProfileLocalDataSourceImpl
    ): UserProfileLocalDataSource

    // Health DataSource
    @Binds
    @Singleton
    abstract fun bindSamsungHealthDataSource(
        impl: SamsungHealthDataSourceImpl
    ): SamsungHealthDataSource

    // Remote DataSource
    @Binds
    @Singleton
    abstract fun bindDrinkRemoteDataSource(
        impl: DrinkRemoteDataSourceImpl
    ): DrinkRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindGeminiRemoteDataSource(
        impl: GeminiRemoteDataSourceImpl
    ): GeminiRemoteDataSource
}