//package com.snoffee.wear.receiver
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//
//class BootReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
//            // [참고] 기기 재부팅 시 폰으로부터 설정값을 다시 동기화하거나,
//            android.util.Log.d("BootReceiver", "기기 재부팅 완료 - 알림 스케줄러 재등록 필요")
//            // WorkManager를 상태 체크하여 재등록하는 로직을 이곳에 구현하세요.
//        }
//    }
//}