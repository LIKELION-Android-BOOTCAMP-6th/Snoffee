package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.entity.DrinkEntity
// 음료 로컬 데이터 접근 계약 (인터페이스)
// 실제 구현은 DrinkLocalDataSourceImpl
interface DrinkLocalDataSource{
    // Room에 저장된 음료 데이터 개수 확인 (첫 실행 여부 판단용)
    suspend fun getCount(): Int

    suspend fun getAllDrinks(): List<DrinkEntity>

    // 음료 데이터 저장 (Firestore에서 가져온 데이터 캐싱)
    suspend fun insertDrinks(drinks: List<DrinkEntity>)

    // 음료 검색 + 페이징
    suspend fun searchDrinks(query: String, limit: Int, offset: Int): List<DrinkEntity>

//    suspend fun searchDrinks(query: String): List<DrinkEntity>
}