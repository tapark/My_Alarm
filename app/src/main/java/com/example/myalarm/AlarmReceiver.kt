package com.example.myalarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        createNotificationChannel(context)
        notifyNotification(context)
    }

    private fun createNotificationChannel(context: Context) {
        // oreo 버젼 이상일 경우 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            NotificationManagerCompat.from(context)
                .createNotificationChannel(notificationChannel)
        }
    }

    private fun notifyNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) {
            val build = NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.alarm_on)
                .setContentTitle("뱀이다")
                .setContentText("몸에좋고 맛도좋은 뱀이다")
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            notify(NOTIFICATION_ID, build.build())
        }
    }

    companion object {
        private const val CHANNEL_ID = "1000"
        private const val CHANNEL_NAME = "MyAlarm"
        private const val NOTIFICATION_ID = 100
    }

}