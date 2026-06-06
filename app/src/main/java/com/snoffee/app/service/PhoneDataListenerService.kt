package com.snoffee.app.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.snoffee.app.domain.repository.CaffeineRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PhoneDataListenerService : WearableListenerService() {

    @Inject
    lateinit var caffeineRepository: CaffeineRepository

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            // 워치와 동일한 경로 확인
            if (event.dataItem.uri.path == "/caffeine/residual_state") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                val name = dataMap.getString("name") ?: "알 수 없음"
                val amount = dataMap.getInt("amount")
                val timestamp = dataMap.getLong("timestamp")

                CoroutineScope(Dispatchers.IO).launch {
                    caffeineRepository.processAndInsertCaffeine(name, amount, timestamp)
                }
            }
        }
    }
}