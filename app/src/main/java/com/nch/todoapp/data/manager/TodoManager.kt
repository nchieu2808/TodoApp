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

    suspend fun syncItems() {
        try {
            val remoteData = apiService.fetchRemoteTodos()
            if (remoteData.isNotEmpty()) {
                // Merge remote data into local cache
                localRepService.saveToCache(remoteData)
            }
        } catch (e: Exception) {
            // Log error but don't wipe local data
            android.util.Log.e("TodoManager", "Sync failed", e)
        } finally {
            // Always update the UI state from local cache
            todoItemsState.value = localRepService.getCachedItems().toList()
        }
    }

    suspend fun getItems(): List<TodoItem> {
        return localRepService.getCachedItems()
    }

    suspend fun createItems(item: TodoItem) {
        localRepService.updateCache(item)
        todoItemsState.value = localRepService.getCachedItems().toList()
        apiService.uploadTodo(item)
    }

    suspend fun updateItems(item: TodoItem) {
        localRepService.updateCache(item)
        todoItemsState.value = localRepService.getCachedItems().toList()
        apiService.updateRemoteTodo(item)
    }

    suspend fun deleteItem(id: String) {
        localRepService.removeFromCache(id)
        todoItemsState.value = localRepService.getCachedItems().toList()
        apiService.deleteRemoteTodo(id)
    }
}