package com.example.ch13_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppNameAdapter(private val appNames: List<String>) :
    RecyclerView.Adapter<AppNameAdapter.AppNameViewHolder>() {

    inner class AppNameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAppName: TextView = itemView.findViewById(R.id.tvAppNameSimple)
        fun bind(name: String) {
            tvAppName.text = name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppNameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_name, parent, false)
        return AppNameViewHolder(view)
    }

    override fun getItemCount(): Int = appNames.size

    override fun onBindViewHolder(holder: AppNameViewHolder, position: Int) {
        holder.bind(appNames[position])
    }
}
