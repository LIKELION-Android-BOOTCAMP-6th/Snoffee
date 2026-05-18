package com.snoffee.app.presentation.caffeine.input

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class CaffeineInputViewModel @Inject constructor(
    // private val searchDrinkUseCase: SearchDrinkUseCase,
    // private val saveCaffeineUseCase: SaveCaffeineUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CaffeineInputUiState())
    val uiState: StateFlow<CaffeineInputUiState> = _uiState.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        // TODO: searchDrinkUseCase(query) 연결
    }

    fun onTimeChange(time: LocalTime) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun onRecord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: saveCaffeineUseCase() 연결
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
