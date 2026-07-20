package com.nch.todoapp.data.remote

import com.nch.todoapp.data.model.TodoItem

val FakeToDoList = mutableListOf<TodoItem>(
    TodoItem("1", "A", "a"),
    TodoItem("2", "B", "b"),
    TodoItem("3", "C", "c", true)
)

class FakeAPIService : ApiService{
    private val remoteDb = FakeToDoList

    override fun fetchRemoteTodos(): List<TodoItem> {
        return remoteDb.toList()
    }

    override fun uploadTodo(item: TodoItem): Boolean {
        remoteDb.add(item)
        return true
    }

    override fun updateRemoteTodo(item: TodoItem): Boolean {
        val index = remoteDb.indexOfFirst { it.id == item.id }
        if (index != -1) {
            remoteDb[index] = item
            return true
        }
        return false
    }
}