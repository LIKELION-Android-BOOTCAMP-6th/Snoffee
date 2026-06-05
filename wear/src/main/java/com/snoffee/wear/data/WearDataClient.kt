package com.snoffee.wear.data

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.logging.Logger

class WearDataClient(private val context: Context) : DataClient.OnDataChangedListener {

    private val logger = Logger.getLogger("WearDataClient")
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val CAPABILITY_PHONE_APP = "verify_snoffee_phone_app"
        private const val PATH_RESIDUAL_STATE = "/caffeine/residual_state"
    }

    private val capabilityClient = Wearable.getCapabilityClient(context)
    private val dataClient = Wearable.getDataClient(context)

    // [AC-1] 실시간 핸드폰 연결 상태 흐름
    private val _isPhoneConnected = MutableStateFlow(false)
    val isPhoneConnected: StateFlow<Boolean> = _isPhoneConnected.asStateFlow()

    // [AC-3] 에러 팝업 제어용 메시지 흐름
    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError: StateFlow<String?> = _connectionError.asStateFlow()

    // [AC-4] 모바일 단독 알림Fallback 활성화 검증 플래그
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

    /**
     * [AC-1] 워치 앱 실행 시 최초 폰 가용성 확인 (Handshake)
     */
    fun checkPhoneCapability() {
        scope.launch {
            try {
                // tasks.await() 에러가 여기서 발생했던 부분입니다.
                val capabilityInfo: CapabilityInfo = capabilityClient
                    .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_REACHABLE)
                    .await()
                updateConnectionState(capabilityInfo)
            } catch (e: Exception) {
                logger.severe("⚠️ Capability 핸드셰이크 실패 (블루투스 단절 예외): ${e.message}")
                handleDisconnect()
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
            logger.info("🟢 [AC-2] 폰 통신 노드가 정상 가역 노드에 위치함.")
        } else {
            handleDisconnect()
        }
    }

    private fun handleDisconnect() {
        _isPhoneConnected.value = false
        _connectionError.value = "핸드폰 유실 또는 연결이 끊어졌습니다."
        _isFallbackActive.value = true
        logger.warning("⚠️ [Edge Case] 기기 단절 감지. Fallback 모바일 단독 알림 제어 플래그를 가동합니다.")
    }

    /**
     * [AC-2] 데이터 레이어 패킷 변동 리스너 수신 파싱 로그 기록
     */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == PATH_RESIDUAL_STATE) {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val residual = dataMap.getDouble("residualCaffeineMg", 0.0)
                val risk = dataMap.getString("riskLevel", "SAFE")
                val time = dataMap.getString("metabolismTime", "--:--")
                val level = dataMap.getString("concentrationLevel", "-")

                logger.info(" 워치 수신 로그 완료. 잔량: ${residual}mg, 위험도: $risk")

                scope.launch {
                    _receivedCaffeineData.emit(
                        mapOf(
                            "residualCaffeineMg" to residual,
                            "riskLevel" to risk,
                            "metabolismTime" to time,
                            "concentrationLevel" to level
                        )
                    )
                }
            }
        }
    }

    fun release() {
        dataClient.removeListener(this)
    }
}