package com.example.jetpack_compose_foot_calendar.ui.profile

import androidx.lifecycle.ViewModel
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val _favoriteTeam = MutableStateFlow<Team?>(null)
    val favoriteTeam: StateFlow<Team?> = _favoriteTeam

    fun setFavoriteTeam(team: Team) {
        _favoriteTeam.value = team
    }

    fun clearFavoriteTeam() {
        _favoriteTeam.value = null
    }
}