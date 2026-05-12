package com.snoffee.app.data.repository

import com.snoffee.app.data.datasource.local.CaffeineLocalDataSource
import com.snoffee.app.data.mapper.CaffeineMapper
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.repository.CaffeineRepository
import javax.inject.Inject

// CaffeineRepository 인터페이스 구현체
// CaffeineLocalDataSource를 통해 Room DB에 접근
// CaffeineMapper를 통해 DTO ↔ Domain Model 변환
class CaffeineRepositoryImpl @Inject constructor(
    private val localDataSource: CaffeineLocalDataSource,  // Hilt가 자동 주입
    private val mapper: CaffeineMapper                     // Hilt가 자동 주입
) : CaffeineRepository {

    override suspend fun saveCaffeineRecord(record: CaffeineRecord) {
        // TODO: Domain Model → DTO 변환 후 저장
        val entity = mapper.toEntity(record)
        localDataSource.insertCaffeineRecord(entity)
    }

    override suspend fun getTodayCaffeineRecords(): List<CaffeineRecord> {
        // Flow → List 변환 필요 (추후 처리)
        return emptyList()
    }

    override suspend fun deleteCaffeineRecord(id: Long) {
        localDataSource.deleteCaffeineRecord(id)
    }
}