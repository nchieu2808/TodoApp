package com.nch.todoapp

import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.local.LocalRepServiceImpl
import com.nch.todoapp.data.manager.TodoManager
import com.nch.todoapp.data.remote.ApiService
import com.nch.todoapp.data.remote.FakeAPIService
import com.nch.todoapp.data.remote.FirebaseApiService

class AppContainer {
    private val localService: LocalRepService by lazy { LocalRepServiceImpl() }
//    private val remoteApi: ApiService by lazy { FakeAPIService() }
    private val remoteApi: ApiService by lazy { FirebaseApiService() }

    val todoManager: TodoManager by lazy { TodoManager(localService, remoteApi) }
}