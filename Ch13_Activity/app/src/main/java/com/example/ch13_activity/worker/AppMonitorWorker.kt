package com.example.ch13_activity.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.ch13_activity.R
import java.util.Calendar

class AppMonitorWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val selectedApp = inputData.getString("selectedApp") ?: return Result.failure()
        val startTime = inputData.getLong("startTime", 0)
        val endTime = inputData.getLong("endTime", 0)
        val repeatType = inputData.getString("repeatType") ?: "반복 안함"

        val now = Calendar.getInstance()

        val isViolation = when (repeatType) {
            "반복 안함" -> {
                val nowMillis = System.currentTimeMillis()
                nowMillis in startTime..endTime
            }
            "매일" -> {
                val hour = now.get(Calendar.HOUR_OF_DAY)
                val minute = now.get(Calendar.MINUTE)
                val nowMinutes = hour * 60 + minute

                val startCal = Calendar.getInstance().apply { timeInMillis = startTime }
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }

                val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
                val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

                nowMinutes in startMinutes..endMinutes
            }
            "매주" -> {
                val currentDay = now.get(Calendar.DAY_OF_WEEK)

                val startCal = Calendar.getInstance().apply { timeInMillis = startTime }
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }

                val targetDay = startCal.get(Calendar.DAY_OF_WEEK)
                if (currentDay != targetDay) return Result.success()

                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
                val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

                nowMinutes in startMinutes..endMinutes
            }

            "매월" -> {
                val today = now.get(Calendar.DAY_OF_MONTH)
                val startDay = Calendar.getInstance().apply { timeInMillis = startTime }.get(Calendar.DAY_OF_MONTH)

                if (today != startDay) return Result.success()

                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val startMinutes = Calendar.getInstance().apply { timeInMillis = startTime }.let {
                    it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
                }
                val endMinutes = Calendar.getInstance().apply { timeInMillis = endTime }.let {
                    it.get(Calendar.HOUR_OF_DAY) * 60 + it.get(Calendar.MINUTE)
                }

                nowMinutes in startMinutes..endMinutes
            }

            "매년" -> {
                val nowYearDay = now.get(Calendar.DAY_OF_YEAR)
                val startCal = Calendar.getInstance().apply { timeInMillis = startTime }
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }

                val targetDay = startCal.get(Calendar.DAY_OF_YEAR)
                if (nowYearDay != targetDay) return Result.success()

                val nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                val startMinutes = startCal.get(Calendar.HOUR_OF_DAY) * 60 + startCal.get(Calendar.MINUTE)
                val endMinutes = endCal.get(Calendar.HOUR_OF_DAY) * 60 + endCal.get(Calendar.MINUTE)

                nowMinutes in startMinutes..endMinutes
            }

            else -> false
        }
        if (isViolation){
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
