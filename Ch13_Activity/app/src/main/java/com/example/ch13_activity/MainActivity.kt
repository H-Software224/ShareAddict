package com.example.ch13_activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ch13_activity.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    var datas: MutableList<Todo>? = null
    lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            it.data?.let { data ->
                val title = data.getStringExtra("title") ?: ""
                val start = data.getStringExtra("startTime") ?: ""
                val end = data.getStringExtra("endTime") ?: ""
                val days = data.getStringArrayListExtra("days") ?: arrayListOf()

                val todo = Todo(title, start, end, days)
                datas?.add(todo)
                adapter.notifyItemInserted(datas!!.size - 1)
            }
        }

        binding.mainFab.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            requestLauncher.launch(intent)
        }

        datas = mutableListOf()
        adapter = MyAdapter(datas)
        binding.mainRecyclerView.adapter = adapter
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.mainRecyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        )
    }

    override fun onResume() {
        super.onResume()
        val today = getTodayLabel() // 예: "수"

        datas?.forEach { todo ->
            if (!todo.days.contains(today)) {
                Toast.makeText(this, "⚠️ 요일 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            } else if (!isNowWithinRange(todo.startTime, todo.endTime)) {
                Toast.makeText(this, "⚠️ 시간 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isNowWithinRange(start: String, end: String): Boolean {
        return try {
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())
            val now = format.parse(format.format(Date()))!!
            val startTime = format.parse(start)
            val endTime = format.parse(end)
            now >= startTime && now <= endTime
        } catch (e: Exception) {
            false
        }
    }

    private fun getTodayLabel(): String {
        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return days[dayOfWeek - 1]
    }
}
