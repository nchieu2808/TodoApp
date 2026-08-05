package com.nch.todoapp.data.auth

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isGuest: Boolean = false
)
