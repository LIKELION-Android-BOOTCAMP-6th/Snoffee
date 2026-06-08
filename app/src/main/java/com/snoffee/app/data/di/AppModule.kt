package com.snoffee.app.di

import android.content.Context
import com.snoffee.app.data.wear.PhoneDataClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePhoneDataClient(@ApplicationContext context: Context): PhoneDataClient {
        return PhoneDataClient(context)
    }
}