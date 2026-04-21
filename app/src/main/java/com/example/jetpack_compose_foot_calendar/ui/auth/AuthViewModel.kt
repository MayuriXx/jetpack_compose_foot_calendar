package com.example.jetpack_compose_foot_calendar.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun login(username: String, password: String) {
        // Validation basique — comme dans ton authService.login()
        if (username.isBlank() || password.length < 3) {
            _error.value = "Identifiants invalides"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            delay(1000) // simule un appel réseau
            _user.value = User(id = "1", username = username)
            _isAuthenticated.value = true

            _isLoading.value = false
        }
    }

    fun logout() {
        _user.value = null
        _isAuthenticated.value = false
    }
}