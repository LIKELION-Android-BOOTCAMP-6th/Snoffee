package com.snoffee.wear.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import com.snoffee.wear.data.WearDataClient
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WearDataListenerService : WearableListenerService() {

    @Inject
    lateinit var wearDataClient: WearDataClient

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // WearDataClient가 수신 로직 처리
        wearDataClient.onDataChanged(dataEvents)
    }
}