package com.snoffee.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

// 카페인 섭취 기록 테이블
// 피그마 DB 설계 기반으로 생성
@Entity(tableName = "caffeine_record")
data class CaffeineEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                           // PK, 자동생성

    @ColumnInfo(name = "drink_id")
    val drinkId: String,                        // 음료 ID

    @ColumnInfo(name = "drink_name")
    val drinkName: String,                      // 음료명

    @ColumnInfo(name = "brand_name")
    val brandName: String,                      // 브랜드명

    @ColumnInfo(name = "intake_size")
    val intakeSize: Double,                     // 섭취 용량

    @ColumnInfo(name = "intake_caffeine")
    val intakeCaffeine: Double,                 // 섭취 카페인 (mg)

    @ColumnInfo(name = "consumed_at")
    val consumedAt: Long                         // 섭취 시각
)