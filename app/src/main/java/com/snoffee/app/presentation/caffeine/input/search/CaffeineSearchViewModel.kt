package com.snoffee.app.presentation.caffeine.input.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.model.DrinkItem
import com.snoffee.app.domain.usecase.caffeine.SaveCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CaffeineSearchViewModel @Inject constructor(
    // private val searchDrinkUseCase: SearchDrinkUseCase,
    private val saveCaffeineUseCase: SaveCaffeineUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaffeineSearchUiState())
    val uiState: StateFlow<CaffeineSearchUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // TODO: searchDrinkUseCase(query) 연결
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    // 검색한 음료 선택
    fun onDrinkSelected(drink: DrinkItem) {
        _uiState.update { it.copy(selectedDrink = drink) }
    }

    // 사용자가 직접 입력한 음료 or Firestore 검색으로 선택한 음료 저장
    fun saveCaffeineRecord(record: CaffeineRecord) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                saveCaffeineUseCase(record)
                _uiState.update { it.copy(isLoading = false, isSaved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
