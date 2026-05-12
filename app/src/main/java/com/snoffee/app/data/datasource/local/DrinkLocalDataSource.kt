package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.entity.DrinkEntity
// 음료 로컬 데이터 접근 계약 (인터페이스)
// 실제 구현은 DrinkLocalDataSourceImpl
interface DrinkLocalDataSource{
    suspend fun getAllDrinks(): List<DrinkEntity>
    suspend fun insertDrinks(drinks: List<DrinkEntity>)
    suspend fun searchDrinks(query: String): List<DrinkEntity>
}