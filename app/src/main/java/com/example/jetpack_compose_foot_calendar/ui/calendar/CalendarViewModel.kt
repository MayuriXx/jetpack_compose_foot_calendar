package com.example.jetpack_compose_foot_calendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.domain.model.League
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    // ─── État brut ────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Match>>> = _uiState

    // ─── Filtres ──────────────────────────────────────────────────

    private val _selectedStatus = MutableStateFlow("all")
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _selectedCountry = MutableStateFlow("all")
    val selectedCountry: StateFlow<String> = _selectedCountry

    private val _selectedLeague = MutableStateFlow("all")
    val selectedLeague: StateFlow<String> = _selectedLeague

    // ─── Données dérivées ─────────────────────────────────────────

    // Liste des pays disponibles — équivalent de ton computed countries
    val countries: StateFlow<List<String>> = _uiState
        .map { state ->
            if (state is UiState.Success) {
                state.data
                    .map { it.league.country }
                    .distinct()
                    .sorted()
            } else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Liste des ligues filtrées par pays — équivalent de ton computed leagues
    val leagues: StateFlow<List<League>> = combine(
        _uiState, _selectedCountry
    ) { state, country ->
        if (state is UiState.Success) {
            state.data
                .map { it.league }
                .distinctBy { it.id }
                .filter { country == "all" || it.country == country }
                .sortedBy { it.name }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Matchs filtrés — équivalent de ton computed filteredMatches
    val filteredMatches: StateFlow<List<Match>> = combine(
        _uiState, _selectedStatus, _selectedCountry, _selectedLeague
    ) { state, status, country, league ->
        if (state !is UiState.Success) return@combine emptyList()

        state.data.filter { match ->
            val statusOk = status == "all" || match.status.name.lowercase() == status
            val countryOk = country == "all" || match.league.country == country
            val leagueOk = league == "all" || match.league.id.toString() == league
            statusOk && countryOk && leagueOk
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Matchs groupés par ligue — équivalent de ton filteredMatchesByLeague
    val matchesByLeague: StateFlow<Map<League, List<Match>>> = filteredMatches
        .map { matches ->
            matches.groupBy { it.league }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // ─── Actions ──────────────────────────────────────────────────

    init {
        loadTodayMatches() // chargement automatique au démarrage
    }

    fun loadTodayMatches() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTodayMatches().fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
    }

    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    fun setCountryFilter(country: String) {
        _selectedCountry.value = country
        _selectedLeague.value = "all" // reset la ligue quand on change de pays
    }

    fun setLeagueFilter(league: String) {
        _selectedLeague.value = league
    }

    fun resetFilters() {
        _selectedStatus.value = "all"
        _selectedCountry.value = "all"
        _selectedLeague.value = "all"
    }
}