package com.nch.todoapp.data.model

import com.google.errorprone.annotations.Keep

@Keep
data class TodoItem(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false
)