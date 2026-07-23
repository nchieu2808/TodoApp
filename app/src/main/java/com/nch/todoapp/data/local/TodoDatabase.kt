package com.nch.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nch.todoapp.data.model.TodoItem

@Database(entities = [TodoItem::class], version = 2, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}