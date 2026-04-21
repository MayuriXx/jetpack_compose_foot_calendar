package com.example.jetpack_compose_foot_calendar.ui.matchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.domain.model.MatchDetail
import com.example.jetpack_compose_foot_calendar.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the match detail screen.
 *
 * Exposes a single [uiState] flow that transitions through [UiState.Loading],
 * [UiState.Success], and [UiState.Error] as the fixture detail is fetched from
 * [FootballRepository].
 *
 * **Note:** The corresponding `MatchDetailScreen` composable is not yet implemented.
 * The ViewModel is already instantiated in [AppNavGraph] ready for when the screen is added.
 *
 * @param repository The data source used to fetch fixture detail.
 */
class MatchDetailViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MatchDetail>>(UiState.Loading)

    /** Current loading/success/error state of the match detail request. */
    val uiState: StateFlow<UiState<MatchDetail>> = _uiState

    /**
     * Loads the full detail for the given fixture.
     *
     * Sets [uiState] to [UiState.Loading], then to [UiState.Success] on success or
     * [UiState.Error] on failure.
     *
     * @param fixtureId The unique fixture identifier to fetch.
     */
    fun loadMatchDetail(fixtureId: Int) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            repository.getMatchDetail(fixtureId).fold(
                onSuccess = { _uiState.value = UiState.Success(it) },
                onFailure = { _uiState.value = UiState.Error(it.message ?: "Erreur inconnue") }
            )
        }
    }
}