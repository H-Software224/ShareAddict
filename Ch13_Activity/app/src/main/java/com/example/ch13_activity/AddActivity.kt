package com.example.ch13_activity

import android.app.Activity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.ch13_activity.databinding.ActivityAddBinding

class AddActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_add_save -> {
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
            dayMap.forEach { (button, label) ->
                if (button.isChecked) selectedDays.add(label)
            }

            val resultIntent = intent
            resultIntent.putExtra("title", binding.addEditView.text.toString())
            resultIntent.putExtra("startTime", binding.startTimeEditText.text.toString())
            resultIntent.putExtra("endTime", binding.endTimeEditText.text.toString())
            resultIntent.putStringArrayListExtra("days", ArrayList(selectedDays))
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
