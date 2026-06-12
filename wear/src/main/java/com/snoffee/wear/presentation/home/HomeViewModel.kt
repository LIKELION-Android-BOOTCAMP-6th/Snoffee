package com.snoffee.wear.presentation.home

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoffee.wear.data.WearDataClient
import com.snoffee.wear.domain.model.CaffeineRecord
import com.snoffee.wear.domain.usecase.CalculateResidualUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val wearDataClient: WearDataClient,
    private val calculateResidualUseCase: CalculateResidualUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WatchHomeUiState())
    val uiState: StateFlow<WatchHomeUiState> = _uiState.asStateFlow()

    private val prefs = context.getSharedPreferences("caffeine_prefs", Context.MODE_PRIVATE)

    private var lastSensitivity: String
        get() = prefs.getString("user_sensitivity", "NORMAL") ?: "NORMAL"
        set(value) {
            prefs.edit { putString("user_sensitivity", value) }
        }

    // 로컬 캐시
    private val localRecordCache = mutableListOf<CaffeineRecord>()
    private var localCalculationJob: Job? = null

    init {
        observeDataFlow()
    }

    private fun observeDataFlow() {
        viewModelScope.launch {
            wearDataClient.isPhoneConnected.collectLatest { isConnected ->
                if (isConnected) {
                    localCalculationJob?.cancel()
                    observePhoneData()
                } else {
                    startLocalCalculationLoop()
                }
            }
        }
    }

    private suspend fun observePhoneData() {
        wearDataClient.receivedCaffeineData.collectLatest { dataMap ->
            if (dataMap.isNotEmpty()) {
                lastSensitivity = dataMap["sensitivity"] as? String ?: "NORMAL"

                val caffeineMg = (dataMap["residualMg"] as? Number)?.toDouble() ?: 0.0
                _uiState.update {
                    it.copy(
                        residualCaffeineMg = caffeineMg,
                        riskLevel = runCatching {
                            CaffeineRiskLevel.valueOf(dataMap["riskLevel"] as String)
                        }.getOrDefault(CaffeineRiskLevel.SAFE),
                        metabolismTime = dataMap["metabolismTime"] as? String ?: "--:--",
                        isLoading = false,
                        isDataEmpty = false
                    )
                }
            }
        }
    }

    private fun startLocalCalculationLoop() {
        localCalculationJob = viewModelScope.launch {
            while (true) {
                val analysis = calculateResidualUseCase(
                    records = localRecordCache,
                    halfLifeHours = lastSensitivity.toHalfLife(),
                    now = System.currentTimeMillis()
                )

                _uiState.update {
                    it.copy(
                        residualCaffeineMg = analysis.residualAmount,
                        metabolismTime = formatTime(analysis.cutoffTime),
                        isLoading = false
                    )
                }
                delay(60000) // 1분마다 재계산
            }
        }
    }

    fun addRecord(name: String, amount: Double) {
        val newRecord = CaffeineRecord(name, amount, System.currentTimeMillis())
        localRecordCache.add(newRecord)

        viewModelScope.launch {
            val success =
                wearDataClient.sendCustomCaffeineRecord(name, amount.toInt(), newRecord.time)
            if (!success) {
                Log.w("HomeViewModel", "폰 연결 안 됨: 로컬 캐시에 저장됨")
            }
        }
    }

    private fun formatTime(timeMillis: Long): String {
        return SimpleDateFormat("HH:mm", Locale.KOREA).format(Date(timeMillis))
    }
}