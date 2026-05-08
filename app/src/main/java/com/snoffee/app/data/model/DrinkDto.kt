package com.snoffee.app.data.model

// 음료 데이터 DTO
// Firebase에서 가져온 데이터 형태. DrinkMapper를 통해 DrinkItem으로 변환
data class DrinkDto(
    val foodId: String,                     // 음료 ID (Firebase PK)
    val name: String,                       // 음료명
    val category: String,                   // 카테고리
    val brand: String,                      // 브랜드명
    val caffeineMg: Double,                 // 기준 용량당 카페인 함량 (mg)
    val caffeinePer: Double,                // 기준 용량
    val totalCaffeine: Double,              // 전체 용량당 카페인 함량
    val servingSize: Double,                // 전체 용량
    val servingUnit: String                 // 단위 (ml, g 등)
)