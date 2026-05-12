package com.snoffee.app.data.mapper

import com.snoffee.app.data.local.entity.DrinkEntity
import com.snoffee.app.data.model.DrinkDto
import com.snoffee.app.domain.model.DrinkItem
import javax.inject.Inject

// DrinkDto ↔ DrinkItem 변환
class DrinkMapper @Inject constructor() {
    //Firebase DTO -> Domain 모델 (UI용)
    fun mapToDomain(dto: DrinkDto): DrinkItem {
        return DrinkItem(
            foodId = dto.foodId,
            name = dto.name,
            category = dto.category,
            brand = dto.brand,
            caffeineMg = dto.caffeinemg,
            servingSize = dto.serving_size,
            totalCaffeine = dto.total_caffeine,
            totalSize = dto.total_size
        )
    }

    //Room Entity -> Domain 모델 (UI용)
    fun mapToDomain(entity: DrinkEntity): DrinkItem {
        return DrinkItem(
            foodId = entity.foodId,
            name = entity.name,
            category = entity.category,
            brand = entity.brand,
            caffeineMg = entity.caffeineMg,
            servingSize = entity.servingSize,
            totalCaffeine = entity.totalCaffeine,
            totalSize = entity.totalSize
        )
    }

    //리스트 변환
    //fun mapToDomainList(dtoList: List<DrinkDto>) = dtoList.map { mapToDomain(it) }

    //Firebase DTO -> Room Entity (로컬 저장용)
    fun mapToEntity(dto: DrinkDto): DrinkEntity {
        return DrinkEntity(
            foodId = dto.foodId,
            name = dto.name,
            category = dto.category,
            brand = dto.brand,
            caffeineMg = dto.caffeinemg,
            servingSize = dto.serving_size,
            totalCaffeine = dto.total_caffeine,
            totalSize = dto.total_size
        )
    }
}