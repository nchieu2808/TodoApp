package com.nch.todoapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nch.todoapp.data.model.TodoItem

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY isCompleted ASC")
    suspend fun getAllItems(): List<TodoItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TodoItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateItem(item: TodoItem)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM todos")
    suspend fun clearAll()
}