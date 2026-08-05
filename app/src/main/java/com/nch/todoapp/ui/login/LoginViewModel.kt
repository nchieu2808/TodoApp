package com.nch.todoapp.ui.login

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.nch.todoapp.data.auth.AuthRepository
import com.nch.todoapp.data.auth.AuthUser
import com.nch.todoapp.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val currentUser: StateFlow<AuthUser?> = authRepository.currentUser

    override fun getScreenName(): String = "Login_Screen"

    fun signInWithGoogle(context: Context) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.signInWithGoogle(context)
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Google sign-in failed"
                }
            _isLoading.value = false
        }
    }

    fun signInAsGuest() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            authRepository.signInAsGuest()
                .onFailure { e ->
                    _errorMessage.value = e.message ?: "Guest sign-in failed"
                }
            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
