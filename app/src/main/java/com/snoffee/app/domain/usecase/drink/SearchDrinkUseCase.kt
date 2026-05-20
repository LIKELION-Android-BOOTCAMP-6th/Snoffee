package com.snoffee.app.domain.usecase.drink

import com.snoffee.app.domain.model.DrinkItem
import com.snoffee.app.domain.repository.DrinkRepository
import javax.inject.Inject

// 음료 검색 UseCase (Room 기반 contains 검색 + 페이징)
class SearchDrinkUseCase @Inject constructor(
    private val drinkRepository: DrinkRepository,
) {
    suspend operator fun invoke(
        query: String,
        page: Int = 0,
        pageSize: Int = 20,
    ): List<DrinkItem> = drinkRepository.searchDrinks(
        query = query,
        page = page,
        pageSize = pageSize,
    )
}