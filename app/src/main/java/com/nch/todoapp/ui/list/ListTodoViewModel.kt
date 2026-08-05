package com.nch.todoapp.ui.list

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TodoFilter {
    ALL, COMPLETED, NOT_COMPLETED
}

@HiltViewModel
class ListTodoViewModel @Inject constructor(
    private val todoManager: TodoManager
) : BaseViewModel() {

    private val _pendingDeletions = MutableStateFlow<Set<String>>(emptySet())
    private val _filter = MutableStateFlow(TodoFilter.ALL)
    val filter: StateFlow<TodoFilter> = _filter

    val todoList: StateFlow<List<TodoItem>> = combine(
        todoManager.todoItemsState,
        _pendingDeletions,
        _filter
    ) { todos, pending, currentFilter ->
        todos.filter { it.id !in pending }
            .filter {
                when (currentFilter) {
                    TodoFilter.ALL -> true
                    TodoFilter.COMPLETED -> it.isCompleted
                    TodoFilter.NOT_COMPLETED -> !it.isCompleted
                }
            }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    override fun getScreenName(): String = "Todo_List_Screen"

    fun setFilter(filter: TodoFilter) {
        _filter.value = filter
    }

    fun loadTodos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                todoManager.syncItems()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load tasks."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleTodoCompletion(item: TodoItem) {
        viewModelScope.launch {
            try {
                val updated = item.copy(isCompleted = !item.isCompleted)
                todoManager.updateItems(updated)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update task."
            }
        }
    }

    fun hideTodo(item: TodoItem) {
        _pendingDeletions.value += item.id
    }

    fun undoHide(item: TodoItem) {
        _pendingDeletions.value -= item.id
    }

    fun commitDelete(item: TodoItem) {
        viewModelScope.launch {
            try {
                todoManager.deleteItem(item.id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete task."
                undoHide(item)
            } finally {
                _pendingDeletions.value -= item.id
            }
        }
    }
}
