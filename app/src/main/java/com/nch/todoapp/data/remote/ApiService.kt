package com.nch.todoapp.data.remote

import com.nch.todoapp.data.model.TodoItem

interface ApiService {
    suspend fun fetchRemoteTodos(): List<TodoItem>
    suspend fun uploadTodo(item: TodoItem): Boolean
    suspend fun updateRemoteTodo(item: TodoItem): Boolean
    suspend fun deleteRemoteTodo(id: String): Boolean
}