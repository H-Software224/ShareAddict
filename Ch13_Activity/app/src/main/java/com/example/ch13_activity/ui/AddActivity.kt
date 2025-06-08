package com.example.ch13_activity.ui

import com.example.ch13_activity.worker.AppMonitorWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import android.app.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.example.ch13_activity.R
import com.example.ch13_activity.data.AppDatabase
import com.example.ch13_activity.data.AppRule
import com.example.ch13_activity.databinding.ActivityAddBinding
import com.example.ch13_activity.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class AddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBinding

    private var startMillis: Long = 0L
    private var endMillis: Long = 0L
    private var selectedAppPackage: String? = null

    data class AppInfo(val label: String, val packageName: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        // 날짜 및 시간 선택
        binding.btnPickStart.setOnClickListener { pickDateTime(true) }
        binding.btnPickEnd.setOnClickListener { pickDateTime(false) }

        // 앱 선택 버튼 클릭
        binding.ivArrow.setOnClickListener {
            val pm = packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .map {
                    AppInfo(
                        label = pm.getApplicationLabel(it).toString(),
                        packageName = it.packageName
                    )
                }.sortedBy { it.label }

            val view = layoutInflater.inflate(R.layout.dialog_app_list, null)
            val listView = view.findViewById<ListView>(R.id.app_list_view)
            val labels = apps.map { it.label }

            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, labels)
            listView.adapter = adapter

            val dialog = AlertDialog.Builder(this)
                .setTitle("앱을 선택하세요")
                .setView(view)
                .create()

            listView.setOnItemClickListener { _, _, position, _ ->
                val selectedApp = apps[position]
                selectedAppPackage = selectedApp.packageName
                binding.tvSelectApp.text = "선택됨: ${selectedApp.label}"
                dialog.dismiss()
            }

            dialog.show()
        }

        // 저장 버튼 클릭
        binding.btnSave.setOnClickListener {
            // DB 저장 로직 추가
            val db = AppDatabase.getDatabase(applicationContext)
            val rule = AppRule(
                packageName = selectedAppPackage ?: "",
                startTimeMillis = startMillis,
                endTimeMillis = endMillis,
                appName = binding.tvSelectApp.text.toString().removePrefix("선택됨: "),
                startHour = Calendar.getInstance().apply { timeInMillis = startMillis }.get(Calendar.HOUR_OF_DAY),
                startMinute = Calendar.getInstance().apply { timeInMillis = startMillis }.get(Calendar.MINUTE),
                endHour = Calendar.getInstance().apply { timeInMillis = endMillis }.get(Calendar.HOUR_OF_DAY),
                endMinute = Calendar.getInstance().apply { timeInMillis = endMillis }.get(Calendar.MINUTE),
                rules = binding.addEditView.text.toString()
            )

            val selectedDays = mutableListOf<String>()
            val dayMap = mapOf(
                binding.dayMon to "월",
                binding.dayTue to "화",
                binding.dayWed to "수",
                binding.dayThu to "목",
                binding.dayFri to "금",
                binding.daySat to "토",
                binding.daySun to "일"
            )
            dayMap.forEach { (btn, label) ->
                if (btn.isChecked) selectedDays.add(label)
            }
            val resultIntent = intent.apply {
                putExtra("title", binding.addEditView.text.toString())
                putExtra("startDateTime", startMillis)
                putExtra("endDateTime", endMillis)
                putExtra("selectedApp", selectedAppPackage)
                putStringArrayListExtra("days", ArrayList(selectedDays))
            }
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    db.appRuleDao().insertRule(rule)
                    try {
                        val response = RetrofitClient.api.uploadRule(rule)
                        if (response.isSuccessful) {
                            val bodyString = response.body()?.string()
                            Log.d("AddActivity",  "✅ 서버 전송 성공 $bodyString")
                        } else {
                            val errorString = response.errorBody()?.string()
                            Log.e("AddActivity", "❌ 서버 응답 실패: ${response.code()} / $errorString")
                        }
                    } catch (e: Exception) {
                        Log.e("AddActivity", "❌ 서버 요청 실패", e)
                    }
                }

                withContext(Dispatchers.Main) {
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
            val data = Data.Builder()
                .putString("selectedApp", selectedAppPackage)
                .putLong("startTime", startMillis)
                .putLong("endTime", endMillis)
                .build()

            val request = PeriodicWorkRequestBuilder<AppMonitorWorker>(15, TimeUnit.MINUTES)
                .setInputData(data)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AppMonitor",
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )


            // ✅ 위반 시간 체크 + 알림 테스트
            checkViolationAndNotify(this, selectedAppPackage ?: "", startMillis, endMillis)
        }
    }

    // 날짜 및 시간 선택
    private fun pickDateTime(isStart: Boolean) {
        // 사용자 직접 선택 유도: 현재 시간 자동 설정 없음
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val cal = Calendar.getInstance()
                cal.set(year, month, day, hour, minute)

                if (isStart) {
                    startMillis = cal.timeInMillis
                    binding.tvRangePreview.text = "시작: ${Date(startMillis)}"
                } else {
                    endMillis = cal.timeInMillis
                    binding.tvRangePreview.append("\n종료: ${Date(endMillis)}")
                }
            }, 12, 0, true).show()
        }, 2025, 0, 1).show()
    }

    // ✅ 위반 시간에 알림 발생
    private fun checkViolationAndNotify(context: Context, selectedPackage: String, start: Long, end: Long) {
        val now = System.currentTimeMillis()
        if (now in start..end) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "violation-channel"
            val channel = NotificationChannel(
                channelId,
                "Violation Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "앱 미사용 시간대 위반 알림"
            }
            manager.createNotificationChannel(channel)
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.small) // 🔔 아이콘 반드시 필요!
                .setContentTitle("사용 금지 앱 감지됨")
                .setContentText("앱 [$selectedPackage]이 금지 시간에 열렸습니다.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            manager.notify(1001, builder.build())
        }
    }
}
