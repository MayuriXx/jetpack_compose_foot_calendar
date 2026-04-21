package com.example.jetpack_compose_foot_calendar.domain.model

/**
 * Represents an authenticated user.
 *
 * **Note:** Authentication is currently simulated locally (no real backend). This model
 * is only used by [AuthViewModel] to hold session state in memory.
 *
 * @property id       Unique user identifier (set to `"1"` by the mock login).
 * @property username The display name entered by the user on the login screen.
 * @property email    Optional email address; not collected by the current login flow.
 */
data class User(
    val id: String,
    val username: String,
    val email: String? = null
)
