package com.snoffee.app.presentation.caffeine.input.search

import com.snoffee.app.domain.model.DrinkItem
import java.time.LocalTime

data class CaffeineSearchUiState(
    val searchQuery: String = "",
    val searchResults: List<DrinkItem> = emptyList(),
    val selectedDrink: DrinkItem? = null,
    val selectedTime: LocalTime = LocalTime.now(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
) {
    val isRecordEnabled: Boolean
        get() = selectedDrink != null && !isLoading
}
