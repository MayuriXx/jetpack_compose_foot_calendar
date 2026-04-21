package com.example.jetpack_compose_foot_calendar.ui.profile

import androidx.lifecycle.ViewModel
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel for the user profile screen.
 *
 * Manages the user's favourite team selection in memory. This state is not persisted across
 * app restarts in the current implementation.
 *
 * **Note:** The corresponding `ProfileScreen` composable is not yet implemented.
 * This ViewModel is instantiated in [AppNavGraph] ready for when the screen is added.
 */
class ProfileViewModel : ViewModel() {

    private val _favoriteTeam = MutableStateFlow<Team?>(null)

    /** The user's currently selected favourite team, or `null` if none has been set. */
    val favoriteTeam: StateFlow<Team?> = _favoriteTeam

    /**
     * Sets the user's favourite team.
     *
     * @param team The [Team] to mark as favourite.
     */
    fun setFavoriteTeam(team: Team) {
        _favoriteTeam.value = team
    }

    /** Clears the user's favourite team selection. */
    fun clearFavoriteTeam() {
        _favoriteTeam.value = null
    }
}