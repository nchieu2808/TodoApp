package com.nch.todoapp

import android.content.Context
import androidx.room.Room
import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.local.LocalRepServiceImpl
import com.nch.todoapp.data.local.TodoDatabase
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.remote.ApiService
import com.nch.todoapp.data.remote.FakeAPIService
import com.nch.todoapp.data.remote.FirebaseApiService

class AppContainer(private val context: Context) {

    private val database: TodoDatabase by lazy {
        Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            "todo-database"
        ).fallbackToDestructiveMigration().build()
    }

    private val localService: LocalRepService by lazy {
        LocalRepServiceImpl(database.todoDao())
    }

    private val remoteApi: ApiService by lazy { FirebaseApiService() }

    val todoManager: TodoManager by lazy { TodoManager(localService, remoteApi) }
}