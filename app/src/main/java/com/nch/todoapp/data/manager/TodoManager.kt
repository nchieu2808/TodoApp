package com.nch.todoapp.data.manager

import android.util.Log
import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoManager @Inject constructor(
    private val localRepService: LocalRepService,
    private val apiService: ApiService
) {
    val todoItemsState = MutableStateFlow<List<TodoItem>>(emptyList())

    private val mutex = Mutex()

    suspend fun syncItems() = mutex.withLock {
        try {
            val remoteData = apiService.fetchRemoteTodos()
            // Remote is source of truth on successful fetch (including empty list).
            localRepService.saveToCache(remoteData)
        } catch (e: Exception) {
            // Keep local cache when remote is unavailable.
            Log.e(TAG, "Sync failed", e)
        } finally {
            refreshStateLocked()
        }
    }

    suspend fun getItems(): List<TodoItem> = localRepService.getCachedItems()

    suspend fun createItems(item: TodoItem) = mutex.withLock {
        if (!apiService.uploadTodo(item)) {
            throw IOException("Failed to save task to remote storage")
        }
        localRepService.updateCache(item)
        refreshStateLocked()
    }

    suspend fun updateItems(item: TodoItem) = mutex.withLock {
        if (!apiService.updateRemoteTodo(item)) {
            throw IOException("Failed to update task on remote storage")
        }
        localRepService.updateCache(item)
        refreshStateLocked()
    }

    suspend fun deleteItem(id: String) = mutex.withLock {
        if (!apiService.deleteRemoteTodo(id)) {
            throw IOException("Failed to delete task on remote storage")
        }
        localRepService.removeFromCache(id)
        refreshStateLocked()
    }

    suspend fun clearSession() = mutex.withLock {
        localRepService.clearCache()
        todoItemsState.value = emptyList()
    }

    private suspend fun refreshStateLocked() {
        todoItemsState.value = localRepService.getCachedItems().toList()
    }

    companion object {
        private const val TAG = "TodoManager"
    }
}
