package com.snoffee.wear.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.wear.data.WearDataClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
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
        viewModelScope.launch {
            // 연결 확인 후 요청
            wearDataClient.requestSyncFromPhone()
        }
    }

    private fun observePhoneData() {
        viewModelScope.launch {
            wearDataClient.receivedCaffeineData.collectLatest { dataMap ->
                if (dataMap.isNotEmpty()) {
                    val rawCaffeine = dataMap["residualMg"]
                    val caffeineMg = (rawCaffeine as? Number)?.toDouble()

                    _uiState.update { currentState ->
                        currentState.copy(
                            residualCaffeineMg = caffeineMg ?: currentState.residualCaffeineMg,
                            riskLevel = (dataMap["riskLevel"] as? String)?.let {
                                runCatching { CaffeineRiskLevel.valueOf(it) }.getOrDefault(
                                    currentState.riskLevel
                                )
                            } ?: currentState.riskLevel,
                            metabolismTime = (dataMap["metabolismTime"] as? String)
                                ?: currentState.metabolismTime,
                            concentrationLevel = (dataMap["concentrationLevel"] as? String)
                                ?: currentState.concentrationLevel,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun retryConnection() {
        wearDataClient.checkPhoneCapability()
    }
}