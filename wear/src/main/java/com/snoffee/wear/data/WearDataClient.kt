package com.snoffee.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.logging.Logger


@Singleton
class WearDataClient @Inject constructor(
    @param:ApplicationContext private val context: Context
) : DataClient.OnDataChangedListener {

    private val logger = Logger.getLogger("WearDataClient")
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val CAPABILITY_PHONE_APP = "verify_snoffee_phone_app"
        private const val PATH_RESIDUAL_STATE = "/caffeine/residual_state"
        const val PATH_RECENT_DRINKS = "/caffeine/recent_drinks"
        private const val PATH_ADD_RECORD = "/caffeine/add_record"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val dataClient = Wearable.getDataClient(context)

    //실시간 핸드폰 연결 상태 흐름
    private val _isPhoneConnected = MutableStateFlow(true)
    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()

    //에러 팝업 제어용 메시지 흐름
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    //모바일 단독 알림Fallback 활성화 검증 플래그
    private val _isFallbackActive = MutableStateFlow(false)
    val isFallbackActive: StateFlow<Boolean> = _isFallbackActive.asStateFlow()

    // UI 레이아웃 바인딩용 수신 데이터 구조 파싱 스트림
    private val _receivedCaffeineData = MutableStateFlow<Map<String, Any>>(emptyMap())
    val receivedCaffeineData: StateFlow<Map<String, Any>> = _receivedCaffeineData.asStateFlow()

    init {
        dataClient.addListener(this)
        checkPhoneCapability()
        setupCapabilityListener()
    }

    //워치 앱 실행 시 최초 폰 가용성 확인 (Handshake)
    fun checkPhoneCapability() {
        scope.launch {
            try {
                val capabilityInfo: CapabilityInfo = capabilityClient
                    .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
                    .await()
                updateConnectionState(capabilityInfo)
            } catch (e: Exception) {
                handleDisconnect()
            }
        }
    }

    fun sendMessage(path: String, data: ByteArray) {
        scope.launch {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                Wearable.getMessageClient(context).sendMessage(node.id, path, data).await()
            }
        }
    }
    private fun setupCapabilityListener() {
        capabilityClient.addListener({ capabilityInfo ->
            updateConnectionState(capabilityInfo)
        }, CAPABILITY_PHONE_APP)
    }

    private fun updateConnectionState(capabilityInfo: CapabilityInfo) {
        if (capabilityInfo.nodes.isNotEmpty()) {
            _isPhoneConnected.value = true
            _connectionError.value = null
            _isFallbackActive.value = false
            logger.info("폰 통신 노드가 정상 가역 노드에 위치함.")
        } else {
            handleDisconnect()
        }
    }

    private fun handleDisconnect() {
        _isPhoneConnected.value = false
        _connectionError.value = "핸드폰 유실 또는 연결이 끊어졌습니다."
        _isFallbackActive.value = true
        scope.launch { _receivedCaffeineData.emit(emptyMap()) }
    }

    //데이터 레이어 패킷 변동 리스너 수신 파싱 로그 기록
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            val uri = event.dataItem.uri.path
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                try {
                    // 카페인 잔류 상태 수신 로직 (예외 처리: 데이터 누락/타입 불일치 대비)
                    if (uri == PATH_RESIDUAL_STATE) {
                        // mapOf 대신 안전한 Map 구조 생성
                        val residualMap = mapOf(
                            "residualMg" to (dataMap.getDouble("residualCaffeineMg", 0.0)),
                            "riskLevel" to (dataMap.getString("riskLevel") ?: "SAFE"),
                            "metabolismTime" to (dataMap.getString("metabolismTime") ?: "--:--"),
                            "concentrationLevel" to (dataMap.getString("concentrationLevel") ?: "-")
                        )
                        scope.launch { _receivedCaffeineData.emit(residualMap) }
                    }
                } catch (e: Exception) {
                    logger.severe("⚠️ 데이터 레이어 파싱 중 오류 발생: ${e.message}")
                }
            }
        }
    }
    suspend fun sendCustomCaffeineRecord(name: String, amount: Int, consumedAt: Long): Boolean {
        if (!_isPhoneConnected.value) return false
        return try {
            val request = PutDataMapRequest.create("/caffeine/add_record").apply {
                dataMap.putString("name", name)
                dataMap.putInt("amount", amount)
                dataMap.putLong("timestamp", consumedAt)
                dataMap.putLong("update_time", System.currentTimeMillis())
            }.asPutDataRequest().setUrgent()

            com.google.android.gms.tasks.Tasks.await(dataClient.putDataItem(request))
            true
        } catch (e: Exception) {
            logger.severe("❌ 패킷 전송 실패: ${e.message}")
            false
        }
    }

    suspend fun requestSyncFromPhone() {
        try {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            nodes.forEach { node ->
                Wearable.getMessageClient(context)
                    .sendMessage(node.id, "/caffeine/request_sync", null).await()
            }
            Log.d("WearDataClient", "🚀 폰으로 데이터 동기화 요청을 보냈습니다.")
        } catch (e: Exception) {
            Log.e("WearDataClient", "❌ 동기화 요청 실패: ${e.message}")
        }
    }

    fun release() {
        dataClient.removeListener(this)
    }
}