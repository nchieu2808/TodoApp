package com.nch.todoapp.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.nch.todoapp.data.auth.SessionManager
import com.nch.todoapp.data.model.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Firestore-backed ApiService. Kept for when DI switches away from LocalFileApiService.
 * Todos are stored under users/{uid}/todos/{todoId}.
 */
class FirebaseApiService @Inject constructor(
    private val sessionManager: SessionManager,
    private val db: FirebaseFirestore
) : ApiService {

    private fun todoCollection() =
        db.collection("users")
            .document(sessionManager.requireUserId())
            .collection("todos")
    override suspend fun fetchRemoteTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        withTimeout(10_000L.milliseconds) {
            val snapshot = todoCollection().get().await()
            val items = snapshot.toObjects(TodoItem::class.java)
            Log.d(TAG, "Fetched ${items.size} todos")
            items
        }
    }

    override suspend fun uploadTodo(item: TodoItem): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(10_000L.milliseconds) {
                todoCollection().document(item.id).set(item).await()
                Log.d(TAG, "Successfully uploaded todo: ${item.id}")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading todo: ${item.id}", e)
            false
        }
    }

    override suspend fun updateRemoteTodo(item: TodoItem): Boolean = uploadTodo(item)

    override suspend fun deleteRemoteTodo(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeout(10_000L) {
                todoCollection().document(id).delete().await()
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting todo: $id", e)
            false
        }
    }

    companion object {
        private const val TAG = "FirebaseApiService"
    }
}
