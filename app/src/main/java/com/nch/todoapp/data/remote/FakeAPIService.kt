package com.nch.todoapp.data.remote

import com.nch.todoapp.data.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val FakeToDoList = mutableListOf<TodoItem>(
    TodoItem("1", "A", "a"),
    TodoItem("2", "B", "b"),
    TodoItem("3", "C", "c", true)
)

class FakeAPIService : ApiService{
    private val remoteDb = FakeToDoList

    override suspend fun fetchRemoteTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        remoteDb.sortedBy { it.isCompleted }
    }

    override suspend fun uploadTodo(item: TodoItem): Boolean = withContext(Dispatchers.IO) {
        remoteDb.add(item)
        true
    }

    override suspend fun updateRemoteTodo(item: TodoItem): Boolean = withContext(Dispatchers.IO) {
        val index = remoteDb.indexOfFirst { it.id == item.id }
        if (index != -1) {
            remoteDb[index] = item
            true
        } else {
            false
        }
    }

    override suspend fun deleteRemoteTodo(id: String): Boolean = withContext(Dispatchers.IO) {
        remoteDb.removeAll { it.id == id }
        true
    }
}