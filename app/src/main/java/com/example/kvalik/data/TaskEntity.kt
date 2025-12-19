package com.example.kvalik.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val priority: Int, // 0-низкий, 1-средний, 2-высокий
    val isDone: Boolean = false
)
