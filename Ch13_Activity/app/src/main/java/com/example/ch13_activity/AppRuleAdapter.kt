package com.example.ch13_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ch13_activity.data.AppRule

class AppRuleAdapter(
    private val onItemClicked: (AppRule) -> Unit
) : RecyclerView.Adapter<AppRuleAdapter.ViewHolder>() {

    private var rules: List<AppRule> = emptyList()

    fun submitList(newList: List<AppRule>) {
        rules = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppName)
        private val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        private val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)
        private val tvRules: TextView = itemView.findViewById(R.id.tvRules)
        private val tvViolationStatus: TextView = itemView.findViewById(R.id.tvViolationStatus)

        fun bind(rule: AppRule) {
            tvAppName.text = "앱: ${rule.appName}"
            tvStartTime.text = "시작: ${rule.startHour}:${rule.startMinute.toString().padStart(2, '0')}"
            tvEndTime.text = "종료: ${rule.endHour}:${rule.endMinute.toString().padStart(2, '0')}"
            tvRules.text = "규칙: ${rule.rules}"

            // 현재 시각 기준으로 위반 여부 판단
            val now = System.currentTimeMillis()
            val violation = now in rule.startTimeMillis..rule.endTimeMillis
            tvViolationStatus.text = "위반 유무: ${if (violation) "위반함" else "정상"}"

            itemView.setOnClickListener {
                onItemClicked(rule)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_rule, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = rules.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(rules[position])
    }
}
