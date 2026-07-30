package com.nch.todoapp.ui.list

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TodoFilter {
    ALL, COMPLETED, NOT_COMPLETED
}

class ListTodoViewModel(private val todoManager: TodoManager) : BaseViewModel() {

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

    // Optimistically hide the item
    fun hideTodo(item: TodoItem) {
        _pendingDeletions.value += item.id
    }

    // Restore the item to the list
    fun undoHide(item: TodoItem) {
        _pendingDeletions.value -= item.id
    }

    // Actually perform the deletion on the server/DB
    fun commitDelete(item: TodoItem) {
        viewModelScope.launch {
            todoManager.deleteItem(item.id)
            _pendingDeletions.value -= item.id
        }
    }
}
