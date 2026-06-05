package com.snoffee.app.data.wear

import android.content.Context
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.logging.Logger

class PhoneDataClient(context: Context) {

    private val logger = Logger.getLogger("PhoneDataClient")
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dataClient = Wearable.getDataClient(context)

    companion object {
        // 요구사항 명세에 정의된 타겟 공통 경로(Path)
        const val PATH_RESIDUAL_STATE = "/caffeine/residual_state"
    }

    /**
     * 워치 디바이스로 최신 잔류 카페인 데이터 동기화 패킷 전송
     */
    fun sendCaffeineStateToWatch(
        residualMg: Double,
        riskLevel: String,
        metabolismTime: String,
        concentrationLevel: String
    ) {
        scope.launch {
            try {
                val dataMapArgs = PutDataMapRequest.create(PATH_RESIDUAL_STATE).apply {
                    dataMap.putDouble("residualCaffeineMg", residualMg)
                    dataMap.putString("riskLevel", riskLevel)
                    dataMap.putString("metabolismTime", metabolismTime)
                    dataMap.putString("concentrationLevel", concentrationLevel)
                    // 동일 데이터 구조인 경우에도 유실 없이 수신을 강제하기 위해 타임스탬프 추가
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }

                val putDataReq = dataMapArgs.asPutDataRequest()
                val result = dataClient.putDataItem(putDataReq).await()
                logger.info("📱 [Phone ➔ Watch] 데이터 동기화 패킷 전송 성공: ${result.uri.path}")
            } catch (e: Exception) {
                logger.severe("❌ [Phone ➔ Watch] 패킷 전송 실패 (Data Layer 가용 불가): ${e.message}")
            }
        }
    }
}