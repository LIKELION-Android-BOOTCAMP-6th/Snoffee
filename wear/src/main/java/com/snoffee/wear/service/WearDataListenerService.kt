package com.snoffee.wear.service

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService

class WearDataListenerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.dataItem.uri.path == "/caffeine/status") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap

                val residual = dataMap.getDouble("RESIDUAL")
                val isCutoff = dataMap.getBoolean("IS_CUTOFF")
                val id = dataMap.getInt("NOTIFICATION_ID")
            }
        }
    }
}