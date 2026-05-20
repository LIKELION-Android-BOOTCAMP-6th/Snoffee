package com.snoffee.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room DB의 "DrinkEntity"라는 이름의 테이블
@Entity(tableName = "DrinkEntity")
data class DrinkEntity(
    @PrimaryKey
    val foodId: String,          // Firestore 문서 ID (food_cd)
    val name: String,            // 음료명
    val category: String,        // 카테고리
    val brand: String,           // 브랜드명
    val caffeineMg: Double,      // 기준 용량당 카페인 함량
    val servingSize: Double,     // 기준 용량
    val totalCaffeine: Double,   // 전체 용량당 카페인 함량
    val totalSize: Double,       // 전체 용량
)