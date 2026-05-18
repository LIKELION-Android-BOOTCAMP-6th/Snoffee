package com.snoffee.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.dao.DrinkDao
import com.snoffee.app.data.local.dao.SleepDao
import com.snoffee.app.data.local.entity.CaffeineEntity
import com.snoffee.app.data.local.entity.DrinkEntity
import com.snoffee.app.data.local.entity.SleepEntity


@Database(
    entities = [
        CaffeineEntity::class,
        DrinkEntity::class,
        SleepEntity::class], version = 2
)
abstract class SnoffeeDatabase : RoomDatabase() {
    abstract fun caffeineDao(): CaffeineDao
    abstract fun drinkDao(): DrinkDao

    abstract fun sleepDao(): SleepDao
}