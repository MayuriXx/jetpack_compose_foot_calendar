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

/**
 * ViewModel for the calendar screen.
 *
 * Loads today's fixtures via [FootballRepository] on initialisation and exposes a reactive
 * filter chain built on top of [StateFlow] and [combine]. Filters (status, country, league)
 * are independent of each other except that changing the country resets the league selection.
 *
 * @param repository The data source used to fetch today's fixtures.
 */
class CalendarViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    // ─── Raw state ────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<UiState<List<Match>>>(UiState.Loading)

    /** Current loading/success/error state of the fixtures request. */
    val uiState: StateFlow<UiState<List<Match>>> = _uiState

    // ─── Filters ──────────────────────────────────────────────────────────────

    private val _selectedStatus = MutableStateFlow("all")

    /** Currently selected status filter value (`"all"`, `"live"`, `"upcoming"`, `"finished"`). */
    val selectedStatus: StateFlow<String> = _selectedStatus

    private val _selectedCountry = MutableStateFlow("all")

    /** Currently selected country filter value, or `"all"` for no country filter. */
    val selectedCountry: StateFlow<String> = _selectedCountry

    private val _selectedLeague = MutableStateFlow("all")

    /** Currently selected league ID filter value, or `"all"` for no league filter. */
    val selectedLeague: StateFlow<String> = _selectedLeague

    // ─── Derived data ─────────────────────────────────────────────────────────

    /**
     * Distinct sorted list of country names derived from the loaded fixtures.
     * Empty while the data is loading or in error state.
     */
    val countries: StateFlow<List<String>> = _uiState
        .map { state ->
            if (state is UiState.Success) {
                state.data.map { it.league.country }.distinct().sorted()
            } else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    /**
     * Distinct sorted list of leagues, filtered by the currently selected country.
     * Empty while the data is loading or in error state.
     */
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

    /**
     * The list of matches that match all currently active filters.
     * Empty while the data is loading or in error state.
     */
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

    /**
     * Filtered matches grouped by their [League], ready for sectioned rendering.
     * Empty while the data is loading or in error state.
     */
    val matchesByLeague: StateFlow<Map<League, List<Match>>> = filteredMatches
        .map { matches -> matches.groupBy { it.league } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyMap())

    // ─── Actions ──────────────────────────────────────────────────────────────

    init {
        loadTodayMatches()
    }

    /**
     * Triggers a fresh load of today's fixtures from the repository.
     *
     * Sets [uiState] to [UiState.Loading] before the request, then transitions to
     * [UiState.Success] or [UiState.Error] depending on the result.
     */
    fun loadTodayMatches() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getTodayMatches().fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
    }

    /**
     * Updates the status filter.
     *
     * @param status One of `"all"`, `"live"`, `"upcoming"`, or `"finished"`.
     */
    fun setStatusFilter(status: String) {
        _selectedStatus.value = status
    }

    /**
     * Updates the country filter and resets the league filter to `"all"`.
     *
     * @param country The country name to filter by, or `"all"` to show all countries.
     */
    fun setCountryFilter(country: String) {
        _selectedCountry.value = country
        _selectedLeague.value = "all"
    }

    /**
     * Updates the league filter.
     *
     * @param league The league ID as a string to filter by, or `"all"` to show all leagues.
     */
    fun setLeagueFilter(league: String) {
        _selectedLeague.value = league
    }

    /** Resets all filters to their default value (`"all"`). */
    fun resetFilters() {
        _selectedStatus.value = "all"
        _selectedCountry.value = "all"
        _selectedLeague.value = "all"
    }
}