package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.DrinkLocalDataSource
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSource
import com.snoffee.app.data.mapper.DrinkMapper
import com.snoffee.app.domain.model.DrinkItem
import com.snoffee.app.domain.repository.DrinkRepository
import javax.inject.Inject

// DrinkRepository 인터페이스 구현체
// Firebase에서 음료 목록을 가져와 Room DB에 캐싱 후 반환
// DrinkMapper를 통해 DTO ↔ Domain Model 변환
class DrinkRepositoryImpl @Inject constructor(
    private val localDataSource: DrinkLocalDataSource,     // Hilt가 자동 주입
    private val remoteDataSource: DrinkRemoteDataSource,   // Hilt가 자동 주입
    private val mapper: DrinkMapper                        // Hilt가 자동 주입
) : DrinkRepository {

    override suspend fun getDrinkList(): List<DrinkItem> {
        // TODO: Firebase에서 가져와 Room에 캐싱 후 반환
        return emptyList()
    }
}