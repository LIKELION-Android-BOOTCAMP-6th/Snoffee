package com.snoffee.app.presentation.caffeine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.app.domain.model.CaffeineRecord
import com.snoffee.app.domain.usecase.caffeine.SaveCaffeineUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaffeineInputViewModel @Inject constructor(
    private val saveCaffeineUseCase: SaveCaffeineUseCase
) : ViewModel() {

    // 저장 상태
    private val _saveState = MutableStateFlow<SaveState>(SaveState.Idle)
    val saveState: StateFlow<SaveState> = _saveState

    fun saveCaffeineRecord(record: CaffeineRecord) {
        viewModelScope.launch {
            _saveState.value = SaveState.Loading
            try {
                saveCaffeineUseCase(record)
                _saveState.value = SaveState.Success

            } catch (e: Exception) {
                _saveState.value = SaveState.Error(e.message ?: "저장 실패")
            }
        }
    }

    fun resetSaveState() {
        _saveState.value = SaveState.Idle
    }
}

sealed class SaveState {
    object Idle : SaveState()
    object Loading : SaveState()
    object Success : SaveState()
    data class Error(val message: String) : SaveState()
}