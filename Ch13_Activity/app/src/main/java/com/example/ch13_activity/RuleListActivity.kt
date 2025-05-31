package com.example.ch13_activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ch13_activity.data.AppDatabase
import com.example.ch13_activity.data.AppRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RuleListActivity : AppCompatActivity() {

    private lateinit var rvRules: RecyclerView
    private lateinit var ruleAdapter: AppRuleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rule_list)

        rvRules = findViewById(R.id.tvRules)
        rvRules.layoutManager = LinearLayoutManager(this)

        ruleAdapter = AppRuleAdapter { rule ->
            println("클릭된 규칙: ${rule.appName}")
        }
        rvRules.adapter = ruleAdapter

        lifecycleScope.launch {
            val rules = loadRulesFromDb()
            ruleAdapter.submitList(rules)
        }
    }

    private suspend fun loadRulesFromDb(): List<AppRule> {
        return withContext(Dispatchers.IO) {
            AppDatabase.getDatabase(applicationContext).appRuleDao().getAllRules()
        }
    }
}
