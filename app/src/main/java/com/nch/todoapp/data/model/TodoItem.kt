package com.nch.todoapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.google.firebase.firestore.PropertyName

@Keep
@Entity(tableName = "todos")
data class TodoItem(
    @PrimaryKey
    var id: String = "",
    var title: String = "",
    var description: String? = null,
    
    @get:PropertyName("isCompleted")
    @set:PropertyName("isCompleted")
    var isCompleted: Boolean = false,
    
    var imageUrl: String? = null,
    var dueDate: Long? = null
)
