//package com.snoffee.wear.receiver
//
//import android.app.NotificationManager
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import com.google.android.gms.wearable.Wearable
//import java.nio.charset.StandardCharsets
//
//class NotificationDismissReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)
//
//        if (notificationId != -1) {
//            // 워치 로컬 알림 센터에서 즉시 알림 삭제
//            val notificationManager =
//                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.cancel(notificationId)
//
//            // 연동된 스마트폰 알림 센터에서도 동시에 삭제되도록 동기화 메시지 송신
//            val messageClient = Wearable.getMessageClient(context)
//            val dismissPath = "/notification/dismiss"
//            val payload = notificationId.toString().toByteArray(StandardCharsets.UTF_8)
//
//            // 폰 연동 해제용 가이드라인 적용
//            messageClient.sendMessage("PHONE_NODE_ID_HERE", dismissPath, payload)
//        }
//    }
//}