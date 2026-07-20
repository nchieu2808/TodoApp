package com.nch.todoapp.ui.details

import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetailsViewModel(private val todoManager: TodoManager) : BaseViewModel() {

    private val _todoItem = MutableStateFlow<TodoItem?>(null)
    val todoItem: StateFlow<TodoItem?> = _todoItem.asStateFlow()

    override fun getScreenName(): String = "Todo_Details_Screen"

    fun loadDetails(id: String) {
        _todoItem.value = todoManager.getItems().find { it.id == id }
    }
}