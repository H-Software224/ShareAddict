package com.example.ch13_activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ch13_activity.data.AppDatabase
import com.example.ch13_activity.data.AppRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRuleListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appRuleAdapter: AppRuleAdapter
    private var rules: List<AppRule> = emptyList() // ✅ rules 변수 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_list)

        recyclerView = findViewById(R.id.rvRules) // ✅ XML ID와 일치하게 수정
        recyclerView.layoutManager = LinearLayoutManager(this)

        appRuleAdapter = AppRuleAdapter { rule ->
            Toast.makeText(this, "클릭한 앱: ${rule.appName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = appRuleAdapter

        // DB에서 규칙 불러오기
        fetchAppRules()
    }

    private fun fetchAppRules() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            rules = withContext(Dispatchers.IO) {
                db.appRuleDao().getAllRules()
            }
            appRuleAdapter.submitList(rules)
        }
    }
}
