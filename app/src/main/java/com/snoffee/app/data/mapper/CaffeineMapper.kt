package com.snoffee.app.data.mapper

import com.snoffee.app.data.local.entity.CaffeineEntity
import com.snoffee.app.domain.model.CaffeineRecord
import javax.inject.Inject

class CaffeineMapper @Inject constructor() {
    // Entity → Domain
    fun toDomain(entity: CaffeineEntity): CaffeineRecord {
        return CaffeineRecord(
            id = entity.id,
            drinkId = entity.drinkId,
            drinkName = entity.drinkName,
            brandName = entity.brandName,
            intakeSize = entity.intakeSize,
            intakeCaffeine = entity.intakeCaffeine,
            consumedAt = entity.consumedAt
        )
    }

    // Domain → Entity
    fun toEntity(record: CaffeineRecord): CaffeineEntity {
        return CaffeineEntity(
            id = record.id,
            drinkId = record.drinkId,
            drinkName = record.drinkName,
            brandName = record.brandName,
            intakeSize = record.intakeSize,
            intakeCaffeine = record.intakeCaffeine,
            consumedAt = record.consumedAt
        )
    }
}