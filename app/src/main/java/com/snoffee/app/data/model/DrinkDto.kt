package com.snoffee.app.data.model

import com.google.firebase.firestore.PropertyName

// 음료 데이터 DTO
// Firebase에서 가져온 데이터 형태. DrinkMapper를 통해 DrinkItem으로 변환
// foodId 는 Firestore 문서 ID (food_cd) 를 별도로 주입
data class DrinkDto(
    var foodId: String = "",  // Firestore 문서 ID (DrinkRemoteDataSourceImpl에서 doc.id로 주입)

    @get:PropertyName("name")
    var name: String = "",
    @get:PropertyName("brand")
    var brand: String = "",
    @get:PropertyName("category")
    var category: String = "",
    @get:PropertyName("caffeinemg")
    var caffeinemg: Double = 0.0,
    @get:PropertyName("serving_size")
    var serving_size: Double = 0.0,
    @get:PropertyName("total_caffeine")
    var total_caffeine: Double = 0.0,
    @get:PropertyName("total_size")
    var total_size: Double = 0.0,
)