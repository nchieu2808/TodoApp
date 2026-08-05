package com.nch.todoapp.data.auth

import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the active account id used by data layers.
 * Avoids coupling LocalFileApiService to FirebaseAuth so local guest sessions work offline.
 */
@Singleton
class SessionManager @Inject constructor() {
    @Volatile
    var userId: String? = null
        private set

    fun setUserId(userId: String?) {
        this.userId = userId
    }

    fun requireUserId(): String =
        userId ?: throw IOException("Not signed in")
}
