package com.nch.todoapp.ui.details

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val todoManager: TodoManager
) : BaseViewModel() {

    private val todoId = MutableStateFlow("")

    val todoItem: StateFlow<TodoItem?> =
        combine(todoManager.todoItemsState, todoId) { items, id ->
            items.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    override fun getScreenName(): String = "Todo_Details_Screen"

    fun loadDetails(id: String) {
        todoId.value = id
        viewModelScope.launch {
            if (todoManager.todoItemsState.value.none { it.id == id }) {
                todoManager.syncItems()
            }
        }
    }

    fun updateTodo(title: String, description: String, imageUrl: String?, dueDate: Long?) {
        val currentItem = todoItem.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedItem = currentItem.copy(
                    title = title,
                    description = if (description.isBlank()) null else description,
                    imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl,
                    dueDate = dueDate
                )
                todoManager.updateItems(updatedItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update task."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTodo(id: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                todoManager.deleteItem(id)
                withContext(Dispatchers.Main.immediate) {
                    onDeleted()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete task."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
