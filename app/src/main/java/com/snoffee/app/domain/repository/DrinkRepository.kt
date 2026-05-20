package com.snoffee.app.domain.repository

import com.snoffee.app.domain.model.DrinkItem

// 음료 목록 조회 계약
// 실제 구현은 DrinkRepositoryImpl
// Firebase에서 가져온 데이터를 Room에 캐싱해서 반환
interface DrinkRepository {
    // 음료 검색 + 페이징
    // page: 페이지 번호 (0부터 시작), pageSize: 페이지 크기 (기본 20)
    suspend fun searchDrinks(query: String, page: Int, pageSize: Int = 20): List<DrinkItem>
}