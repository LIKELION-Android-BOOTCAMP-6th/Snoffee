package com.snoffee.app.data.model

// 음료 데이터 DTO
// Firebase에서 가져온 데이터 형태. DrinkMapper를 통해 DrinkItem으로 변환
data class DrinkDto(
    val foodId: String = "",        // 음료 ID (Firebase PK)
    val name: String = "",          // 음료명
    val category: String = "",      // 카테고리
    val brand: String = "",         // 브랜드명
    val caffeinemg: Double = 0.0,    // 기준 용량당 카페인 함량 (mg)
    //Firebase와 필드명 일치
    val serving_size: Double = 0.0,   // 기준 용량
    val total_caffeine: Double = 0.0, // 전체 용량당 카페인 함량
    val total_size: Double = 0.0       // 전체 용량
)