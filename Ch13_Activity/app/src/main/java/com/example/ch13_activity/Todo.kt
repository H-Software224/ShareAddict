package com.example.ch13_activity

data class Todo(
    val title: String,
    val startTime: String,
    val endTime: String,
    val days: List<String>  // ["월", "수", "일"]
)

