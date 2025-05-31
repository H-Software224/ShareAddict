package com.example.ch13_activity

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import com.example.ch13_activity.data.AppDatabase
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ch13_activity.databinding.ActivityMainBinding
import com.example.ch13_activity.ui.AddActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val todoList = mutableListOf<Todo>()
    private lateinit var adapter: MyAdapter

    private val addRuleLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.let { data ->
                val title = data.getStringExtra("title") ?: return@let
                val start = data.getStringExtra("startTime") ?: return@let
                val end = data.getStringExtra("endTime") ?: return@let
                val startDate = data.getLongExtra("startDateTime", 0L)
                val endDate = data.getLongExtra("endDateTime", 0L)
                val days = data.getStringArrayListExtra("days") ?: arrayListOf()

                val todo = Todo(title, days, start, end, startDate, endDate)
                todoList.add(todo)
                adapter.notifyItemInserted(todoList.size - 1)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 규칙 추가 FAB
        findViewById<FloatingActionButton>(R.id.fabAddRule).setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            addRuleLauncher.launch(intent)
        }

        // 규칙 리스트 보기 버튼
        binding.btnRuleList.setOnClickListener {
            val intent = Intent(this, AppRuleListActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        adapter = MyAdapter(todoList)
        binding.recyclerViewRules.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            addItemDecoration(DividerItemDecoration(this@MainActivity, DividerItemDecoration.VERTICAL))
        }

        // 권한 체크
        if (!hasUsageAccessPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        val nowMillis = System.currentTimeMillis()
        val today = getTodayLabel()

        todoList.forEach { todo ->
            if (!todo.days.contains(today)) {
                Toast.makeText(this, "⚠️ 요일 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            } else if (nowMillis !in todo.startDateTime..todo.endDateTime) {
                Toast.makeText(this, "⚠️ 시간 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            }
        }

        loadAppNames()
    }

    private fun getTodayLabel(): String {
        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return days[dayOfWeek - 1]
    }

    private fun hasUsageAccessPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
    private fun loadAppNames() {
        lifecycleScope.launch {
            val rules = withContext(Dispatchers.IO) {
                AppDatabase.getDatabase(applicationContext).appRuleDao().getAllRules()
            }

            val appNames = rules.map { it.appName }.distinct()
            val adapter = AppNameAdapter(appNames)

            findViewById<RecyclerView>(R.id.recyclerViewAppNames).apply {
                layoutManager = LinearLayoutManager(this@MainActivity)
                this.adapter = adapter
            }
        }
    }
}
