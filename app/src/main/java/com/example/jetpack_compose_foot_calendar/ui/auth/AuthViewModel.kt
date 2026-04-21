package com.example.jetpack_compose_foot_calendar.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for authentication state.
 *
 * **Note:** Login is currently simulated with a 1-second delay and no real backend call.
 * Any username is accepted as long as the password is at least 3 characters long.
 *
 * Exposes four [StateFlow]s that the UI observes:
 * - [isAuthenticated] — whether the user is currently logged in.
 * - [user]            — the currently logged-in [User], or `null`.
 * - [isLoading]       — whether a login request is in progress.
 * - [error]           — the latest validation or network error message, or `null`.
 */
class AuthViewModel : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Attempts to log in with the given credentials.
     *
     * Validation rules:
     * - [username] must not be blank.
     * - [password] must be at least 3 characters long.
     *
     * On success, [isAuthenticated] is set to `true` and [user] is populated.
     * The login is simulated with a 1-second coroutine delay.
     *
     * @param username The username entered by the user.
     * @param password The password entered by the user.
     */
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

    /**
     * Logs out the current user.
     *
     * Clears [user] and sets [isAuthenticated] to `false`, which triggers the authentication
     * guard in [AppNavGraph] to redirect to the login screen.
     */
    fun logout() {
        _user.value = null
        _isAuthenticated.value = false
    }
}