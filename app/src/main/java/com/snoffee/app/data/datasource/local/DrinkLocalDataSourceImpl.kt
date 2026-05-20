package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.dao.DrinkDao
import com.snoffee.app.data.local.entity.DrinkEntity
import javax.inject.Inject

// DrinkLocalDataSource 구현체
class DrinkLocalDataSourceImpl @Inject constructor(
    private val drinkDao: DrinkDao
) : DrinkLocalDataSource{
    // Room에 저장된 음료 데이터 개수 확인
    override suspend fun getCount(): Int {
        return drinkDao.getCount()
    }

    // 모든 음료 목록 가져오기
    override suspend fun getAllDrinks(): List<DrinkEntity> {
        return drinkDao.getAllDrinks()
    }

    //음료 데이터 저장하기 (Firestore → Room 캐싱)
    override suspend fun insertDrinks(drinks: List<DrinkEntity>) {
        drinkDao.insertDrinks(drinks)
    }

    // 음료 검색 + 페이징
    override suspend fun searchDrinks(query: String, limit: Int, offset: Int): List<DrinkEntity> {
        return drinkDao.searchDrinks(query, limit, offset)
    }

    //이름으로 음료 검색하기
//    override suspend fun searchDrinks(query: String): List<DrinkEntity> {
//        return drinkDao.searchDrinks(query)
//    }
}