package com.nch.todoapp.data.local

import com.nch.todoapp.data.model.TodoItem

interface LocalRepService {
    suspend fun getCachedItems(): List<TodoItem>
    suspend fun saveToCache(items: List<TodoItem>)
    suspend fun updateCache(item: TodoItem)
    suspend fun removeFromCache(id: String)
}