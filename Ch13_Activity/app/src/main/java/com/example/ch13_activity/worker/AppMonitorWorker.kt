package com.example.ch13_activity.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ch13_activity.R

class AppMonitorWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val selectedApp = inputData.getString("selectedApp") ?: return Result.failure()
        val startTime = inputData.getLong("startTime", 0)
        val endTime = inputData.getLong("endTime", 0)

        val now = System.currentTimeMillis()
        if (now in startTime..endTime) {
            sendViolationNotification(selectedApp)
        }

        return Result.success()
    }

    private fun sendViolationNotification(packageName: String) {
        val channelId = "violation-channel"
        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Violation Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.small)
            .setContentTitle("앱 사용 위반 감지")
            .setContentText("금지 시간에 $packageName 앱이 실행되었습니다.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(101, builder.build())
    }
}
