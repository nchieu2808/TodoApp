package com.nch.todoapp.data.manager

import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.remote.ApiService
import com.nch.todoapp.data.model.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow

class TodoManager (
    private val localRepService: LocalRepService,
    private val apiService: ApiService
){
    val todoItemsState = MutableStateFlow<List<TodoItem>>(emptyList())

    fun syncItems() {
        val remoteData = apiService.fetchRemoteTodos()
        localRepService.saveToCache(remoteData)
        todoItemsState.value = localRepService.getCachedItems().toList()
    }

    fun getItems(): List<TodoItem> {
        return localRepService.getCachedItems()
    }

    fun createItems(item: TodoItem) {
        localRepService.updateCache(item)
        todoItemsState.value = localRepService.getCachedItems().toList()
        apiService.uploadTodo(item)
    }

    fun updateItems(item: TodoItem) {
        localRepService.updateCache(item)
        todoItemsState.value = localRepService.getCachedItems().toList()
        apiService.updateRemoteTodo(item)
    }
}