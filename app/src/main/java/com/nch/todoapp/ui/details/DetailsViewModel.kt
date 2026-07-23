package com.nch.todoapp.ui.details

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsViewModel(private val todoManager: TodoManager) : BaseViewModel() {

    private val _todoItem = MutableStateFlow<TodoItem?>(null)
    val todoItem: StateFlow<TodoItem?> = _todoItem.asStateFlow()

    override fun getScreenName(): String = "Todo_Details_Screen"

    fun loadDetails(id: String) {
        viewModelScope.launch {
            _todoItem.value = todoManager.getItems().find { it.id == id }
        }
    }

    fun updateTodo(title: String, description: String, imageUrl: String?) {
        val currentItem = _todoItem.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val updatedItem = currentItem.copy(
                    title = title,
                    description = if (description.isBlank()) null else description,
                    imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl
                )
                todoManager.updateItems(updatedItem)
                _todoItem.value = updatedItem
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