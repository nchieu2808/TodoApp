package com.nch.todoapp.ui.create

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class CreateTodoViewModel (private val todoManager: TodoManager) : BaseViewModel() {

    override fun getScreenName(): String = "Todo_Create_Screen"

    fun saveTodo(title: String, description: String, onSuccess: () -> Unit) {
        if (title.isBlank()) {
            _errorMessage.value = "Title cannot be empty"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val newTodo = TodoItem(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = description
                )
                todoManager.createItems(newTodo)
                withContext(Dispatchers.Main.immediate) {
                    onSuccess()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save task."
            } finally {
                _isLoading.value = false
            }
        }
    }
}