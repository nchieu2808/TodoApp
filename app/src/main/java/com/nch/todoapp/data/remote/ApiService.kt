package com.nch.todoapp.data.remote

import com.nch.todoapp.data.model.TodoItem

interface ApiService {
    fun fetchRemoteTodos(): List<TodoItem>
    fun uploadTodo(item: TodoItem): Boolean
    fun updateRemoteTodo(item: TodoItem): Boolean
}