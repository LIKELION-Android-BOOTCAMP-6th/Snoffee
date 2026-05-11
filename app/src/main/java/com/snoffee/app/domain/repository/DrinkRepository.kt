package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.DrinkItem

// 음료 목록 조회 계약
// 실제 구현은 DrinkRepositoryImpl
// Firebase에서 가져온 데이터를 Room에 캐싱해서 반환
interface DrinkRepository {
    suspend fun getDrinkList(): List<DrinkItem>             // 음료 목록 조회 (Firebase → Room 캐싱)
}