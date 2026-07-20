package com.nch.todoapp.data.model

data class TodoItem(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)