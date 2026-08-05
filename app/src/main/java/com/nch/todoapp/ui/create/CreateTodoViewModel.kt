package com.nch.todoapp.ui.create

import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTodoViewModel @Inject constructor(private val todoManager: TodoManager) : BaseViewModel() {

    override fun getScreenName(): String = "Todo_Create_Screen"

    fun saveTodo(
        title: String,
        description: String,
        imageUrl: String?,
        dueDate: Long?,
        onSuccess: () -> Unit
    ) {
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
                    description = if (description.isBlank()) null else description,
                    imageUrl = if (imageUrl.isNullOrBlank()) null else imageUrl,
                    dueDate = dueDate
                )
                todoManager.createItems(newTodo)
                withContext(Dispatchers.Main.immediate) {
                    onSuccess()
                }
            } catch (e: Exception) {
                android.util.Log.e("CreateTodoViewModel", "Error saving todo", e)
                _errorMessage.value = "Failed to save task: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}