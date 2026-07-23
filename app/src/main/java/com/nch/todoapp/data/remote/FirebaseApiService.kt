package com.nch.todoapp.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.nch.todoapp.data.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class FirebaseApiService : ApiService {

    private val db = FirebaseFirestore.getInstance()
    private val todoCollection = db.collection("todos")

    override suspend fun fetchRemoteTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        try {
            withTimeout(10000L.milliseconds) {
                val snapshot = todoCollection.get().await()
                snapshot.toObjects(TodoItem::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun uploadTodo(item: TodoItem): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(10000L.milliseconds) {
                todoCollection.document(item.id).set(item).await()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun updateRemoteTodo(item: TodoItem): Boolean {
        return uploadTodo(item)
    }

    override suspend fun deleteRemoteTodo(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(10000L.milliseconds) {
                todoCollection.document(id).delete().await()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}