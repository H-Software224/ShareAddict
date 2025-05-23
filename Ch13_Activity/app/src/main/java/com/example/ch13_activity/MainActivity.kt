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
                val startDate = data.getLongExtra("startDateTime", 0L)
                val endDate = data.getLongExtra("endDateTime", 0L)
                val days = data.getStringArrayListExtra("days") ?: arrayListOf()

                val todo = Todo(title, days, start, end, startDate, endDate)
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
        val nowMillis = System.currentTimeMillis()
        val today = getTodayLabel()

        datas?.forEach { todo ->
            if (!todo.days.contains(today)) {
                Toast.makeText(this, "⚠️ 요일 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            } else if (!(nowMillis in todo.startDateTime..todo.endDateTime)) {
                Toast.makeText(this, "⚠️ 날짜/시간 위반: ${todo.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getTodayLabel(): String {
        val days = listOf("일", "월", "화", "수", "목", "금", "토")
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return days[dayOfWeek - 1]
    }
}
