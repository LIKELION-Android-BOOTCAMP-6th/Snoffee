package com.snoffee.wear.data

import android.content.Context
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WearDataClient @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : DataClient.OnDataChangedListener {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val dataClient = Wearable.getDataClient(context)

    private val _isPhoneConnected = MutableStateFlow(true)
    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()

    // 1. 에러 상태를 관리하는 StateFlow 추가
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    private val _receivedCaffeineData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val receivedCaffeineData: StateFlow<Map<String, Any>> = _receivedCaffeineData.asStateFlow()

    init {
        dataClient.addListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == "/caffeine/residual_state") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val residualMap = mapOf(
                    "residualMg" to dataMap.getDouble("residualCaffeineMg", 0.0),
                    "riskLevel" to (dataMap.getString("riskLevel") ?: "SAFE"),
                    "metabolismTime" to (dataMap.getString("metabolismTime") ?: "--:--")
                )
                scope.launch {
                    _receivedCaffeineData.emit(residualMap)
                    _connectionError.emit(null) // 데이터 수신 성공 시 에러 초기화
                }
            }
        }
    }

    suspend fun sendCustomCaffeineRecord(name: String, amount: Int, consumedAt: Long): Boolean {
        return try {
            val request = PutDataMapRequest.create("/caffeine/add_record").apply {
                dataMap.putString("name", name)
                dataMap.putInt("amount", amount)
                dataMap.putLong("timestamp", consumedAt)
                dataMap.putLong("update_time", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request).await()
            true
        } catch (e: Exception) {
            _connectionError.value = "데이터 전송 실패"
            false
        }
    }

    fun checkPhoneCapability() {
        scope.launch {
            try {
                val nodes = Wearable.getNodeClient(context).connectedNodes.await()
                _isPhoneConnected.value = nodes.isNotEmpty()
                if (nodes.isEmpty()) _connectionError.value = "폰이 연결되어 있지 않습니다."
            } catch (e: Exception) {
                _connectionError.value = "연결 확인 실패"
            }
        }
    }
}