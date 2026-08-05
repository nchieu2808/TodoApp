package com.nch.todoapp.data.remote

import android.content.Context
import android.util.Log
import com.nch.todoapp.data.auth.SessionManager
import com.nch.todoapp.data.model.TodoItem
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Demo "remote" API backed by a per-user JSON file in app-private storage.
 * Path: filesDir/remote_todos_{uid}.json
 */
@Singleton
class LocalFileApiService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val sessionManager: SessionManager
) : ApiService {

    private val mutex = Mutex()

    override suspend fun fetchRemoteTodos(): List<TodoItem> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val file = userFile()
            ensureSeeded(file)
            readTodos(file).sortedWith(
                compareBy<TodoItem> { it.isCompleted }
                    .thenBy { it.dueDate == null }
                    .thenBy { it.dueDate }
            )
        }
    }

    override suspend fun uploadTodo(item: TodoItem): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val file = userFile()
                val todos = readTodos(file).toMutableList()
                todos.removeAll { it.id == item.id }
                todos.add(item)
                writeTodos(file, todos)
                true
            }.getOrElse { e ->
                Log.e(TAG, "Error uploading todo: ${item.id}", e)
                false
            }
        }
    }

    override suspend fun updateRemoteTodo(item: TodoItem): Boolean = uploadTodo(item)

    override suspend fun deleteRemoteTodo(id: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            runCatching {
                val file = userFile()
                val todos = readTodos(file).toMutableList()
                val removed = todos.removeAll { it.id == id }
                if (removed) writeTodos(file, todos)
                removed
            }.getOrElse { e ->
                Log.e(TAG, "Error deleting todo: $id", e)
                false
            }
        }
    }

    private fun userFile(): File {
        val uid = sessionManager.requireUserId()
        return File(context.filesDir, "remote_todos_$uid.json")
    }

    private fun ensureSeeded(file: File) {
        if (file.exists()) return
        writeTodos(
            file,
            listOf(
                TodoItem(id = "1", title = "Welcome", description = "Stored in a local JSON file"),
                TodoItem(id = "2", title = "Create a task", description = "Try adding something new"),
                TodoItem(
                    id = "3",
                    title = "Mark complete",
                    description = "Toggle completion from the list",
                    isCompleted = true
                )
            )
        )
    }

    private fun readTodos(file: File): List<TodoItem> {
        if (!file.exists()) return emptyList()
        return runCatching {
            val array = JSONArray(file.readText())
            buildList {
                for (i in 0 until array.length()) {
                    add(array.getJSONObject(i).toTodoItem())
                }
            }
        }.getOrElse { e ->
            Log.e(TAG, "Error reading ${file.name}", e)
            throw IOException("Failed to read remote todos file", e)
        }
    }

    private fun writeTodos(file: File, todos: List<TodoItem>) {
        val array = JSONArray()
        todos.forEach { array.put(it.toJson()) }
        file.writeText(array.toString(2))
    }

    private fun TodoItem.toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("description", description ?: JSONObject.NULL)
        put("isCompleted", isCompleted)
        put("imageUrl", imageUrl ?: JSONObject.NULL)
        put("dueDate", dueDate ?: JSONObject.NULL)
    }

    private fun JSONObject.toTodoItem(): TodoItem = TodoItem(
        id = optString("id", ""),
        title = optString("title", ""),
        description = optNullableString("description"),
        isCompleted = optBoolean("isCompleted", false),
        imageUrl = optNullableString("imageUrl"),
        dueDate = if (isNull("dueDate")) null else optLong("dueDate")
    )

    private fun JSONObject.optNullableString(key: String): String? =
        if (isNull(key)) null else optString(key).takeIf { it.isNotEmpty() }

    companion object {
        private const val TAG = "LocalFileApiService"
    }
}
