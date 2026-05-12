package com.snoffee.app.domain.model

// 음료 목록 항목
// Firebase에서 가져온 음료 데이터를 도메인에서 사용하는 형태
data class DrinkItem(
    val foodId: String,                     // 음료 ID (Firebase PK)
    val name: String,                       // 음료명
    val category: String,                   // 카테고리
    val brand: String,                      // 브랜드명
    val caffeineMg: Double,                 // 기준 용량당 카페인 함량 (mg)
    val servingSize: Double,                // 기준 용량
    val totalCaffeine: Double,              // 전체 용량당 카페인 함량
    val totalSize: Double                // 전체 용량
)