package com.example.ch13_activity

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.ch13_activity.databinding.ActivityAddBinding
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

            val resultIntent = intent
            resultIntent.putExtra("title", binding.addEditView.text.toString())
            resultIntent.putExtra("startDateTime", startMillis)
            resultIntent.putExtra("endDateTime", endMillis)
            resultIntent.putExtra("selectedApp", selectedAppPackage)
            resultIntent.putStringArrayListExtra("days", ArrayList(selectedDays))

            setResult(Activity.RESULT_OK, resultIntent)

            // ✅ 위반 시간 체크 + 알림 테스트
            checkViolationAndNotify(this, selectedAppPackage ?: "", startMillis, endMillis)

            finish()
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Violation Channel",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "앱 미사용 시간대 위반 알림"
                }
                manager.createNotificationChannel(channel)
            }

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
