package com.example.ch13_activity

data class Todo(
    val title: String,
    val days: List<String>,
    val startTime: String,
    val endTime: String,
    val startDateTime: Long,
    val endDateTime: Long
)
