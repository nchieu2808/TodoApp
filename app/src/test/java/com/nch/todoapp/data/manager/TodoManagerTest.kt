package com.nch.todoapp.data.manager

import android.util.Log
import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.model.TodoItem
import com.nch.todoapp.data.remote.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class TodoManagerTest {

    private lateinit var todoManager: TodoManager
    private val localRepService: LocalRepService = mockk(relaxed = true)
    private val apiService: ApiService = mockk()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        coEvery { Log.e(any(), any(), any()) } returns 0
        todoManager = TodoManager(localRepService, apiService)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `syncItems successful - should replace local cache and state`() = runTest {
        val remoteItems = listOf(
            TodoItem(id = "1", title = "Task 1"),
            TodoItem(id = "2", title = "Task 2")
        )
        coEvery { apiService.fetchRemoteTodos() } returns remoteItems
        coEvery { localRepService.getCachedItems() } returns remoteItems

        todoManager.syncItems()

        coVerify(exactly = 1) { localRepService.saveToCache(remoteItems) }
        coVerify(exactly = 1) { localRepService.getCachedItems() }
        assertEquals(remoteItems, todoManager.todoItemsState.value)
    }

    @Test
    fun `syncItems empty remote - should clear local cache`() = runTest {
        coEvery { apiService.fetchRemoteTodos() } returns emptyList()
        coEvery { localRepService.getCachedItems() } returns emptyList()

        todoManager.syncItems()

        coVerify(exactly = 1) { localRepService.saveToCache(emptyList()) }
        assertEquals(emptyList<TodoItem>(), todoManager.todoItemsState.value)
    }

    @Test
    fun `syncItems network error - should keep local cache`() = runTest {
        val cachedItems = listOf(TodoItem(id = "offline", title = "Offline Task"))
        coEvery { apiService.fetchRemoteTodos() } throws Exception("Network Error")
        coEvery { localRepService.getCachedItems() } returns cachedItems

        todoManager.syncItems()

        coVerify(exactly = 0) { localRepService.saveToCache(any()) }
        assertEquals(cachedItems, todoManager.todoItemsState.value)
    }

    @Test
    fun `createItems remote failure - should not write local cache`() = runTest {
        val item = TodoItem(id = "1", title = "New")
        coEvery { apiService.uploadTodo(item) } returns false

        val result = runCatching { todoManager.createItems(item) }

        assertTrue(result.exceptionOrNull() is IOException)
        coVerify(exactly = 0) { localRepService.updateCache(any()) }
    }

    @Test
    fun `createItems success - uploads then caches`() = runTest {
        val item = TodoItem(id = "1", title = "New")
        coEvery { apiService.uploadTodo(item) } returns true
        coEvery { localRepService.getCachedItems() } returns listOf(item)

        todoManager.createItems(item)

        coVerify(exactly = 1) { apiService.uploadTodo(item) }
        coVerify(exactly = 1) { localRepService.updateCache(item) }
        assertEquals(listOf(item), todoManager.todoItemsState.value)
    }
}
