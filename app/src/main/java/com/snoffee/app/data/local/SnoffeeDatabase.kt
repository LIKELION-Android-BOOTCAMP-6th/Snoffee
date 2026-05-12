package com.snoffee.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.entity.CaffeineEntity
import com.snoffee.app.data.local.dao.DrinkDao
import com.snoffee.app.data.local.entity.DrinkEntity



@Database(entities = [CaffeineEntity::class, DrinkEntity::class], version = 1)
abstract class SnoffeeDatabase : RoomDatabase() {
    abstract fun caffeineDao(): CaffeineDao
    abstract fun drinkDao(): DrinkDao
}