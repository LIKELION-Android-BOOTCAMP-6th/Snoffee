package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.DrinkLocalDataSource
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSource
import com.snoffee.app.data.mapper.DrinkMapper
import com.snoffee.app.data.model.DrinkDto
import com.snoffee.app.data.local.entity.DrinkEntity
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
        //로컬(Room) 데이터를 확인
        val localEntities : List<DrinkEntity> = localDataSource.getAllDrinks()

        return if (localEntities.isEmpty()) {
            //로컬이 비어있으면 리모트(Firebase)에서 get
            val remoteList: List<DrinkDto> = remoteDataSource.fetchDrinkList()

            //DTO를 Entity로 변환해서 로컬에 저장 (캐싱)
            val entitiesToInsert = remoteList.map {dto: DrinkDto -> mapper.mapToEntity(dto)}
            localDataSource.insertDrinks(entitiesToInsert)

            //Domain 모델로 변환 & 반환
            remoteList.map { dto -> mapper.mapToDomain(dto) }
        } else {
            // 5. Entity -> Item 변환 후 반환
            localEntities.map { entity -> mapper.mapToDomain(entity) }
        }
    }

    // 검색 기능 추가 구현
    override suspend fun searchDrinks(query: String): List<DrinkItem> {
        // 검색은 무조건 로컬(Room) 데이터소스에서 수행
        val searchResults = localDataSource.searchDrinks(query)
        return searchResults.map { entity -> mapper.mapToDomain(entity) }
    }
}