package com.nch.todoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.firebase.firestore.PropertyName

@Keep
@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String? = null,
    @get:PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    val imageUrl: String? = null
)
