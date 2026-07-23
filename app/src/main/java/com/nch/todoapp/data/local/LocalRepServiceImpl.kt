package com.nch.todoapp.data.local

import com.nch.todoapp.data.model.TodoItem

class LocalRepServiceImpl(private val todoDao: TodoDao) : LocalRepService {

    override suspend fun getCachedItems(): List<TodoItem> = todoDao.getAllItems()

    override suspend fun saveToCache(items: List<TodoItem>) {
        todoDao.clearAll()
        todoDao.insertAll(items)
    }

    override suspend fun updateCache(item: TodoItem) {
        todoDao.updateItem(item)
    }

    override suspend fun removeFromCache(id: String) {
        todoDao.deleteById(id)
    }
}