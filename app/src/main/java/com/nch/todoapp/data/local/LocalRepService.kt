package com.nch.todoapp.data.local

import com.nch.todoapp.data.model.TodoItem

interface LocalRepService {
    fun getCachedItems(): List<TodoItem>
    fun saveToCache(items: List<TodoItem>)
    fun updateCache(item: TodoItem)
}