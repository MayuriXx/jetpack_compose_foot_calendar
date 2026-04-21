package com.example.jetpack_compose_foot_calendar.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import com.example.jetpack_compose_foot_calendar.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Represents one of the 5 major European leagues. */
data class PopularLeague(val id: Int, val name: String)

/**
 * ViewModel for the user profile screen.
 *
 * Loads teams from [FootballRepository] for the selected league and exposes a filtered
 * list based on [searchQuery]. The favourite team selection is kept in memory.
 */
class ProfileViewModel(private val repository: FootballRepository) : ViewModel() {

    // ─── Popular leagues ──────────────────────────────────────────────────────

    val popularLeagues = listOf(
        PopularLeague(39,  "Premier League"),
        PopularLeague(140, "La Liga"),
        PopularLeague(135, "Serie A"),
        PopularLeague(78,  "Bundesliga"),
        PopularLeague(61,  "Ligue 1"),
    )

    // ─── State ────────────────────────────────────────────────────────────────

    private val _selectedLeagueId = MutableStateFlow(39) // Premier League par défaut
    val selectedLeagueId: StateFlow<Int> = _selectedLeagueId

    private val _teamsState = MutableStateFlow<UiState<List<Team>>>(UiState.Loading)
    val teamsState: StateFlow<UiState<List<Team>>> = _teamsState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _favoriteTeam = MutableStateFlow<Team?>(null)
    val favoriteTeam: StateFlow<Team?> = _favoriteTeam

    /** Teams filtered by [searchQuery]. */
    val filteredTeams: StateFlow<List<Team>> = combine(
        _teamsState, _searchQuery
    ) { state, query ->
        if (state !is UiState.Success) return@combine emptyList()
        if (query.isBlank()) state.data
        else state.data.filter { it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    init {
        loadTeams(_selectedLeagueId.value)
    }

    // ─── Actions ──────────────────────────────────────────────────────────────

    /** Selects a league and loads its teams. */
    fun selectLeague(leagueId: Int) {
        _selectedLeagueId.value = leagueId
        _searchQuery.value = ""
        loadTeams(leagueId)
    }

    /** Updates the search query used to filter teams. */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /** Sets the user's favourite team. */
    fun setFavoriteTeam(team: Team) {
        _favoriteTeam.value = team
    }

    /** Clears the user's favourite team selection. */
    fun clearFavoriteTeam() {
        _favoriteTeam.value = null
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private fun loadTeams(leagueId: Int) {
        viewModelScope.launch {
            _teamsState.value = UiState.Loading
            repository.getTeams(leagueId).fold(
                onSuccess = { _teamsState.value = UiState.Success(it) },
                onFailure = { _teamsState.value = UiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
    }
}