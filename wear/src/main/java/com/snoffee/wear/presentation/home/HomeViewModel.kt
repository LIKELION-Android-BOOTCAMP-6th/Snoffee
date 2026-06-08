package com.snoffee.wear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.wear.data.WearDataClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wearDataClient: WearDataClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchHomeUiState())
    val uiState: StateFlow<WatchHomeUiState> = _uiState.asStateFlow()

    val isPhoneConnected = wearDataClient.isPhoneConnected

    init {
        observePhoneData()
    }

    private fun observePhoneData() {
        viewModelScope.launch {
            wearDataClient.receivedCaffeineData.collectLatest { dataMap ->
                if (dataMap.isNotEmpty()) {
                    _uiState.value = WatchHomeUiState(
                        residualCaffeineMg = (dataMap["residualMg"] as? Double) ?: 0.0,
                        riskLevel = CaffeineRiskLevel.valueOf(
                            (dataMap["riskLevel"] as? String) ?: "SAFE"
                        ),
                        metabolismTime = (dataMap["metabolismTime"] as? String) ?: "--:--",
                        concentrationLevel = (dataMap["concentrationLevel"] as? String) ?: "-",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun retryConnection() {
        wearDataClient.checkPhoneCapability()
    }
}