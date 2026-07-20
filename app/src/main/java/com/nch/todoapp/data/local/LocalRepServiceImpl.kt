package com.nch.todoapp.data.local

import com.nch.todoapp.data.model.TodoItem

class LocalRepServiceImpl : LocalRepService {
    private val memoryCache = mutableListOf<TodoItem>()

    override fun getCachedItems(): List<TodoItem> = memoryCache

    override fun saveToCache(items: List<TodoItem>) {
        memoryCache.clear()
        memoryCache.addAll(items)
    }

    override fun updateCache(item: TodoItem) {
        val index = memoryCache.indexOfFirst { it.id == item.id }
        if (index != -1) memoryCache[index] = item else memoryCache.add(item)
    }
}