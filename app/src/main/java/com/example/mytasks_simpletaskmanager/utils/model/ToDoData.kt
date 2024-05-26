package com.example.mytasks_simpletaskmanager.utils.model

data class ToDoData(
    val taskId: String = "",
    val task: String = "",
    var done: Boolean = false
)
