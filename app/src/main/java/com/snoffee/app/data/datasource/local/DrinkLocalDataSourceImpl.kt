package com.snoffee.app.data.datasource.local

import com.snoffee.app.data.local.dao.DrinkDao
import com.snoffee.app.data.local.entity.DrinkEntity
import javax.inject.Inject

// DrinkLocalDataSource 구현체
class DrinkLocalDataSourceImpl @Inject constructor(
    private val drinkDao: DrinkDao
) : DrinkLocalDataSource{
    // 모든 음료 목록 가져오기
    override suspend fun getAllDrinks(): List<DrinkEntity> {
        return drinkDao.getAllDrinks()
    }

    //음료 데이터 저장하기 (캐싱)
    override suspend fun insertDrinks(drinks: List<DrinkEntity>) {
        drinkDao.insertDrinks(drinks)
    }

    //이름으로 음료 검색하기
    override suspend fun searchDrinks(query: String): List<DrinkEntity> {
        return drinkDao.searchDrinks(query)
    }
}