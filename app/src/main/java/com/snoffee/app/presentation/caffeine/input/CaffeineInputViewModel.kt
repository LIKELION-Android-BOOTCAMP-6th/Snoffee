package com.snoffee.app.presentation.caffeine.input

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.core.util.Utils.toTodayEpochMilli
import com.snoffee.app.data.local.dao.CaffeineDao
import com.snoffee.app.data.local.entity.CaffeineEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class CaffeineInputViewModel @Inject constructor(
    private val caffeineDao: CaffeineDao
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

    fun insertDirectCaffeine(drinkName: String, amount: Double, selectedTime: LocalTime) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val consumedAtTimestamp = selectedTime.toTodayEpochMilli()

                val newRecord = CaffeineEntity(
                    drinkId = "DIRECT_${System.currentTimeMillis()}",
                    drinkName = drinkName,
                    brandName = "직접 입력",
                    intakeSize = 0.0,
                    intakeCaffeine = amount,
                    consumedAt = consumedAtTimestamp
                )

                caffeineDao.insertCaffeineRecord(newRecord)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRecord() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // TODO: saveCaffeineUseCase() 연결
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}
