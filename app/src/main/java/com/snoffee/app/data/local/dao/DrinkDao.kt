package com.snoffee.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.snoffee.app.data.local.entity.DrinkEntity

@Dao
interface DrinkDao {
    @Query("SELECT * FROM DrinkEntity")
    suspend fun getAllDrinks(): List<DrinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    @Query("SELECT * FROM DrinkEntity WHERE name LIKE '%' || :query || '%'")
    suspend fun searchDrinks(query: String): List<DrinkEntity>
}