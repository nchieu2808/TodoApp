package com.nch.todoapp.ui.list

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.ui.base.BaseViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ListTodoViewModel(private val todoManager: TodoManager) : BaseViewModel() {

    val todoList: StateFlow<List<TodoItem>> = todoManager.todoItemsState

    override fun getScreenName(): String = "Todo_List_Screen"

    fun loadTodos(forceRefresh: Boolean = false) {
        if (!forceRefresh && todoList.value.isNotEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                todoManager.syncItems()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load tasks from server."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTodoCompletion(item: TodoItem) {
        viewModelScope.launch {
            val updated = item.copy(isCompleted = !item.isCompleted)
            todoManager.updateItems(updated)
        }
    }
}