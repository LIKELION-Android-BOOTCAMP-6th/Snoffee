package com.snoffee.app.data.di

import android.content.Context
import androidx.room.Room
import com.snoffee.app.data.local.SnoffeeDatabase
import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.dao.DrinkDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // @Provides : Hilt한테 provideSnoffeeDatabase() 함수가 SnoffeeDatabase 만드는 방법을 알려주는 어노테이션
    // @Singleton : 앱 전체에서 딱 1번만 만들고 재사용 (계속 만들 필요 없기 때문)
    @Provides
    @Singleton
    fun provideSnoffeeDatabase(@ApplicationContext context: Context): SnoffeeDatabase {
        return Room.databaseBuilder(
            context,
            SnoffeeDatabase::class.java,
            "snoffee_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideCaffeineDao(db: SnoffeeDatabase): CaffeineDao {
        return db.caffeineDao()
    }

    @Provides
    @Singleton
    fun provideDrinkDao(db: SnoffeeDatabase): DrinkDao {
        return db.drinkDao()
    }
}