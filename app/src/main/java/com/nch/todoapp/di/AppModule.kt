package com.nch.todoapp.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nch.todoapp.data.auth.AuthRepository
import com.nch.todoapp.data.auth.FirebaseAuthRepository
import com.nch.todoapp.data.local.LocalRepService
import com.nch.todoapp.data.local.LocalRepServiceImpl
import com.nch.todoapp.data.local.TodoDao
import com.nch.todoapp.data.local.TodoDatabase
import com.nch.todoapp.data.remote.ApiService
import com.nch.todoapp.data.remote.LocalFileApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase {
        return Room.databaseBuilder(
            context,
            TodoDatabase::class.java,
            "todo-database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    @Provides
    @Singleton
    fun provideTodoDao(database: TodoDatabase): TodoDao = database.todoDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideCredentialManager(@ApplicationContext context: Context): CredentialManager =
        CredentialManager.create(context)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLocalRepService(
        localRepServiceImpl: LocalRepServiceImpl
    ): LocalRepService

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        firebaseAuthRepository: FirebaseAuthRepository
    ): AuthRepository

    // Demo: use local JSON file. Swap to FirebaseApiService when connecting to Firebase.
    @Binds
    @Singleton
    abstract fun bindApiService(
        localFileApiService: LocalFileApiService
        // firebaseApiService: FirebaseApiService
    ): ApiService
}
