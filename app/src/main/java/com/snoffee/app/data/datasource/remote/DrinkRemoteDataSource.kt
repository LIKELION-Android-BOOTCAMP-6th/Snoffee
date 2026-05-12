package com.snoffee.app.data.datasource.remote

import com.snoffee.app.data.model.DrinkDto
// Firebase Drink 데이터 접근 계약 (인터페이스)
interface DrinkRemoteDataSource{
    suspend fun fetchDrinkList(): List<DrinkDto>
}