package com.nch.todoapp.data.auth

import android.content.Context
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentUser: StateFlow<AuthUser?>

    suspend fun signInWithGoogle(context: Context): Result<AuthUser>

    suspend fun signInAsGuest(): Result<AuthUser>

    suspend fun signOut()
}
