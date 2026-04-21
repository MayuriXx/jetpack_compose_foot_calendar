package com.example.jetpack_compose_foot_calendar.ui.matchdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jetpack_compose_foot_calendar.data.repository.FootballRepository
import com.example.jetpack_compose_foot_calendar.domain.model.MatchDetail
import com.example.jetpack_compose_foot_calendar.ui.common.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MatchDetailViewModel(
    private val repository: FootballRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<MatchDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<MatchDetail>> = _uiState

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