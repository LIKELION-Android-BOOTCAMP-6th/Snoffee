package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.DrinkLocalDataSource
import com.snoffee.app.data.datasource.remote.DrinkRemoteDataSource
import com.snoffee.app.data.mapper.DrinkMapper
import com.snoffee.app.data.model.DrinkDto
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
    override suspend fun searchDrinks(query: String, page: Int, pageSize: Int): List<DrinkItem> {
        // Room이 비어있으면 Firestore에서 전체 다운로드 후 캐싱 (첫 실행 1회만)
        if (localDataSource.getCount() == 0) {
            val remoteList: List<DrinkDto> = remoteDataSource.fetchDrinkList()
            val entitiesToInsert = remoteList.map { dto -> mapper.mapToEntity(dto) }
            localDataSource.insertDrinks(entitiesToInsert)
        }

        // Room에서 contains 검색 + LIMIT/OFFSET 페이징
        val offset = page * pageSize
        return localDataSource
            .searchDrinks(query = query, limit = pageSize, offset = offset)
            .map { entity -> mapper.mapToDomain(entity) }
    }
}